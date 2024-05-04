package com.example.ShopApp.sevices;

import com.example.ShopApp.dtos.OrderDTO;
import com.example.ShopApp.dtos.OrderDetailDTO;
import com.example.ShopApp.entity.Order;
import com.example.ShopApp.entity.OrderDetail;
import com.example.ShopApp.entity.Product;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.repositories.OrderDetailRepository;
import com.example.ShopApp.repositories.OrderRepository;
import com.example.ShopApp.repositories.ProductRepository;

import com.example.ShopApp.response.OrderDetailResponse;
import com.example.ShopApp.response.OrderResponse;
import com.example.ShopApp.sevices.impl.OrderDetailServiceImpl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class OrderDetailService implements OrderDetailServiceImpl {
    private final OrderDetailRepository orderDetailRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public OrderDetailResponse createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        Order order = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(()-> new DataNotFoundException(String.format("Order with order's id = %d not found", orderDetailDTO.getOrderId())));
        Product product = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(() -> new DataNotFoundException(String.format("Product with product's id = %d not found", orderDetailDTO.getProductId())));

        OrderDetail orderDetail = OrderDetail.builder()
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor())
                .order(order)
                .price(orderDetailDTO.getPrice())
                .numberOfProducts(orderDetailDTO.getNumberOfProduct())
                .product(product)
                .build();
        orderDetailRepository.save(orderDetail);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
        return orderDetailResponse;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailRepository.findById(id).orElseThrow(() -> new DataNotFoundException(String.format("Order Detail with id = %d not found", id)));
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
        return orderDetailResponse;
    }

    @Override
    public List<OrderDetailResponse> getOrderDetailByOrderId(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails.stream().map(OrderDetailResponse::fromOrderDetail).collect(Collectors.toList());;
        return orderDetailResponses;
    }
    @Transactional
    @Override
    public OrderDetailResponse updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailRepository.findById(id).orElseThrow(() -> new DataNotFoundException(String.format("Order Detail with id = %d not found", id)));
        Product product = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(() -> new DataNotFoundException(String.format("Product with id = %d not found", orderDetailDTO.getProductId())));
        Order order = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(() -> new DataNotFoundException(String.format("Order with id = %d not found", orderDetailDTO.getOrderId())));
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        orderDetail.setPrice(orderDetailDTO.getPrice());
        orderDetail.setColor(orderDetailDTO.getColor());
        orderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProduct());
        orderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());

        orderDetailRepository.save(orderDetail);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
        return orderDetailResponse;
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long id) {
        Optional<OrderDetail> optionalOrderDetail = orderDetailRepository.findById(id);
        if(optionalOrderDetail.isPresent()){
            orderDetailRepository.delete(optionalOrderDetail.get());
        }
    }
}
