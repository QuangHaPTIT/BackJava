package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.dtos.OrderDTO;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderServiceImpl {
    OrderResponse createOrder(OrderDTO orderDTO) throws DataNotFoundException;
    OrderResponse getOrderById(Long id);
    OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;
    void deleteOrder(Long id);
    List<OrderResponse> getOrderByUserId(Long userId);
    Page<OrderResponse> getOrdersByKeyword(String keyword, PageRequest pageRequest);
}
