package com.example.ShopApp.sevices;

import com.example.ShopApp.dtos.ProductDTO;
import com.example.ShopApp.dtos.ProductImageDTO;
import com.example.ShopApp.entity.Category;
import com.example.ShopApp.entity.Product;
import com.example.ShopApp.entity.ProductImage;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.exceptions.InvalidParamException;
import com.example.ShopApp.repositories.CategoryRepository;
import com.example.ShopApp.repositories.ProductImageRepository;
import com.example.ShopApp.repositories.ProductRepository;
import com.example.ShopApp.response.ProductResponse;
import com.example.ShopApp.sevices.impl.ProductSeviceImpl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class ProductService implements ProductSeviceImpl {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public ProductResponse createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find category with id " + productDTO.getCategoryId()));
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .category(existingCategory)
                .description(productDTO.getDescription())
                .active(1)
                .build();
        productRepository.save(newProduct);
        ProductResponse productResponse = ProductResponse.fromProduct(newProduct);
        return productResponse;
    }

    @Override
    public ProductResponse getProductById(Long id) throws DataNotFoundException {

        Product product = productRepository.findById(id).orElseThrow(()-> new DataNotFoundException("Cannot find product with id " + id));
        ProductResponse productResponse = ProductResponse.fromProduct(product);
        return productResponse;
    }

    public List<ProductResponse> findProductByIds(List<Long> productIds){
        List<Product> products = productRepository.findProductByIds(productIds);
        List<ProductResponse> productResponses= ProductResponse.fromListProduct(products);
        return productResponses;
    }

    @Override
    public Page<ProductResponse> getAllProduct(PageRequest pageRequest, String keyword, Long categoryId) {
        // Lấy danh sách sản phẩm theo trang (page), giới hạn (limit), và categoryId (nếu có)
        Page<Product> productPage;
        productPage = productRepository.searchProducts(categoryId, keyword, pageRequest);
        return productPage.map(ProductResponse::fromProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(long id, ProductDTO productDTO) throws DataNotFoundException {
        Optional<Product> existingProduct = productRepository.findById(id);
        if(existingProduct.isPresent()){
            Product product = existingProduct.get();
            if(productDTO.getCategoryId() != null) {
                Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                        .orElseThrow(() -> new DataNotFoundException("Cannot find category with id " + productDTO.getCategoryId()));
                product.setCategory(existingCategory);
            }
            if(productDTO.getName() != null) {
                product.setName(productDTO.getName());
            }
            if(productDTO.getDescription() != null){
                product.setDescription(productDTO.getDescription());
            }
            if(productDTO.getPrice() != null) {
                product.setPrice(productDTO.getPrice());
            }
            if(productDTO.getThumbnail() != null) {
                product.setThumbnail(productDTO.getThumbnail());
            }
            product.onUpdate();
            ProductResponse productResponse = ProductResponse.fromProduct(product);
            productRepository.save(product);
            return productResponse;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deleteProduct(long id) throws DataNotFoundException {
        Optional<Product> existingProduct = productRepository.findById(id);
        if(existingProduct.isPresent()) {
            existingProduct.get().setActive(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean existByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws DataNotFoundException, InvalidParamException {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new DataNotFoundException("Cannot find product with id " + productId));

        ProductImage productImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new InvalidParamException("Number of Images must be <= 5");
        }
        return productImageRepository.save(productImage);
    }
}
