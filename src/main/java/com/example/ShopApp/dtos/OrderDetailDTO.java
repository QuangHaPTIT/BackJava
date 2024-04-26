package com.example.ShopApp.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    @JsonProperty("order_id")
    @Min(value = 1, message = "OrderId must be >= 1")
    private Long orderId;

    @JsonProperty("product_id")
    @Min(value = 1, message = "ProductId  must be >= 1")
    private Long productId;

    @Min(value = 0, message = "price >= 0")
    private Float price;

    @JsonProperty("number_of_product")
    @Min(value = 1, message = "number of product >= 1")
    private int numberOfProduct;

    @JsonProperty("total_money")
    @Min(value = 0, message = "total money >= 0")
    private Float totalMoney;

    private String color;
}
