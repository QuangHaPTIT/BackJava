package com.example.ShopApp.response;

import com.example.ShopApp.entity.Category;
import com.example.ShopApp.entity.Product;
import com.example.ShopApp.entity.ProductImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    @JsonProperty("create_at")
    private LocalDateTime createdAt;
    @JsonProperty("update_at")
    private LocalDateTime updateAt;
    @JsonProperty("category_id")
    private Long categoryId;
    private Category category;
    @JsonProperty("product_images")
    private List<ProductImageResponse> productImages = new ArrayList<>();

    @JsonProperty("total_pages")
    private int totalPages;
    public static ProductResponse fromProduct(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .thumbnail(product.getThumbnail())
                .categoryId(product.getCategory().getId())
                .productImages(fromListProductImage(product.getProductImages()))
                .updateAt(product.getUpdateAt())
                .createdAt(product.getCreateAt())
                .build();
    }

    public static List<ProductResponse> fromListProduct(List<Product> products){
        List<ProductResponse> productResponses= new ArrayList<>();
        for(Product product : products) {
            ProductResponse productResponse = fromProduct(product);
            productResponses.add(productResponse);
        }
        return productResponses;
    }
    private static List<ProductImageResponse> fromListProductImage(List<ProductImage> productImages){
        if(productImages == null) {
            return null;
        }
        List<ProductImageResponse> productResponses = new ArrayList<>();
        for(ProductImage productImage : productImages){
            ProductImageResponse dataImage = ProductImageResponse.fromProductImage(productImage);
            productResponses.add(dataImage);
        }
        return productResponses;
    }
}
