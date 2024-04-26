package com.example.ShopApp.response;

import com.example.ShopApp.entity.ProductImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("image_url")
    private String image;

    public static ProductImageResponse fromProductImage(ProductImage productImage){
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .productId(productImage.getProduct().getId())
                .image(productImage.getImageUrl())
                .build();
    }
}
