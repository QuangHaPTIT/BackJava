package com.example.ShopApp.controllers;


import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.dtos.ProductDTO;
import com.example.ShopApp.dtos.ProductImageDTO;
import com.example.ShopApp.entity.Product;
import com.example.ShopApp.entity.ProductImage;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.ProductImageResponse;
import com.example.ShopApp.response.ProductListResponse;
import com.example.ShopApp.response.ProductResponse;
import com.example.ShopApp.sevices.impl.ProductRedisServiceImpl;
import com.example.ShopApp.sevices.impl.ProductSeviceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductSeviceImpl productSevice;

    private final ProductRedisServiceImpl productRedisService;
    private final LocalizationUtils localizationUtils;
    @GetMapping("")
    public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "12") int limit,
                                            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
                                            @RequestParam(defaultValue = "") String keyword
                                            ) throws JsonProcessingException {
        int totalPages = 0;
        PageRequest pageRequest = PageRequest.of(page - 1, limit,
                Sort.by("id").ascending()
                //Sort.by("createAt").descending()
        );
        logger.info(String.format("keyword: %s, category: %d, page: %d, limit: %d", keyword, categoryId, page, limit));
        List<ProductResponse> productResponses = productRedisService.getAllProducts(keyword, categoryId, pageRequest);
        if (productResponses!=null && !productResponses.isEmpty()) {
            totalPages = productResponses.get(0).getTotalPages();
        }
        if(productResponses == null) {
            Page<ProductResponse> productPage =  productSevice.getAllProduct(pageRequest, keyword, categoryId);
            // Lấy tổng số trang
            totalPages = productPage.getTotalPages();
            productResponses = productPage.getContent();
            for (ProductResponse product : productResponses) {
                product.setTotalPages(totalPages);
            }
            productRedisService.saveAllProducts(productResponses, keyword, categoryId, pageRequest);
        }
        logger.info("Hello");
        return ResponseEntity.ok().body(ProductListResponse.builder()
                .products(productResponses)
                .totalPages(totalPages)
                .build());

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) throws DataNotFoundException {
        ProductResponse product = productSevice.getProductById(id);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductByIds(@RequestParam("ids") String ids){
        try{
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<ProductResponse> productResponses = productSevice.findProductByIds(productIds);
            return ResponseEntity.ok().body(productResponses);
        }catch (Exception error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }

    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult result){

        try{
            if(result.hasErrors()){
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(errors);
            }
            ProductResponse productResponse = productSevice.createProduct(productDTO);
            return ResponseEntity.ok(productResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // dùng để upload file ảnh
    @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> uploadImages(@ModelAttribute("files") List<MultipartFile> files, @PathVariable("id") Long id) throws IOException {
        try {

            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body("You can only upload maximum 5 images with per product");
            }
            ProductResponse existingProduct = productSevice.getProductById(id);
            files = files == null ? new ArrayList<MultipartFile>() : files;
            List<ProductImageResponse> productImages = new ArrayList<>();
            for(MultipartFile file : files){
                if(file.getSize() == 0){
                    continue;
                }
                if(file.getSize() > 10 * 1024 * 1024){
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File is too large");
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("File must be an image");
                }
                String fileName = storeFile(file);
                ProductImage productImage = productSevice.createProductImage(id,
                        ProductImageDTO.builder().imageUrl(fileName).build());
                ProductImageResponse productImageResponse = ProductImageResponse.fromProductImage(productImage);
                // lưu vào đối tượng Product trong DB
                // Lưu vào bảng product_images
                productImages.add(productImageResponse);
            }
            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName){
        try{
            Path imagePath = Paths.get("uploads/" + imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if(resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isImageFile(MultipartFile file){
        String contentType = file.getContentType();
        return contentType != null & contentType.startsWith("image/");
    }

    private String storeFile(MultipartFile file) throws IOException {
        if(!isImageFile(file) && file.getOriginalFilename() == null){
            throw new IOException("Invalid image format");
        }
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())); // Làm sạch file chứa các kí tự lạ
        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        Path uploadDir = Paths.get("uploads");
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        // Đường dẫn đầy đủ đến file
        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO){
        try{
            ProductResponse productResponse = productSevice.updateProduct(id, productDTO);
            return ResponseEntity.ok().body(productResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        try{
            boolean isDeleteSuccess = productSevice.deleteProduct(id);
            if(isDeleteSuccess == false) {
                throw new DataNotFoundException("Product not exists");
            }
            return ResponseEntity.ok(String.format("Product with id = %d deleted successfully", id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }


    @PostMapping("/generateFakeProducts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> generateFakeProducts(){
        Faker faker = new Faker();
        for(int i = 0; i < 1000000; i++){
            String productName = faker.commerce().productName();
            if(productSevice.existByName(productName)){
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 9000000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(1, 4))
                    .build();
            try{
                productSevice.createProduct(productDTO);
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok().body("Fake products created successfully");

    }
}
