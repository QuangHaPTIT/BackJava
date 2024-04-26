package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.dtos.OrderDetailDTO;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.OrderDetailResponse;

import java.util.List;

public interface OrderDetailServiceImpl {
    OrderDetailResponse createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException;
    OrderDetailResponse getOrderDetail(Long id) throws DataNotFoundException;
    List<OrderDetailResponse> getOrderDetailByOrderId(Long orderId);
    OrderDetailResponse updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException;
    void deleteOrderDetail(Long id);
}
