package com.example.ShopApp.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrderListResponse {
    private List<OrderResponse> orderResponses;
    private int totalPages;
}
