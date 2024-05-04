package com.example.ShopApp.sevices;

import com.example.ShopApp.dtos.CartItemDTO;
import com.example.ShopApp.dtos.OrderDTO;
import com.example.ShopApp.dtos.UserDTO;
import com.example.ShopApp.entity.*;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.repositories.OrderDetailRepository;
import com.example.ShopApp.repositories.OrderRepository;
import com.example.ShopApp.repositories.ProductRepository;
import com.example.ShopApp.repositories.UserRepository;
import com.example.ShopApp.response.OrderDetailResponse;
import com.example.ShopApp.response.OrderResponse;
import com.example.ShopApp.response.UserResponse;
import com.example.ShopApp.sevices.impl.OrderServiceImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class OrderService implements OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws DataNotFoundException {

        User user = userRepository.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("User's id not found"));
        // dùng thư viện modal maper Convert OrderDTO -> Order
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper->mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(new Date());

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Data must be at least today");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setStatus(OrderStatus.PENDING);

        // Tạo danh sách các đối tượng OrderDetail từ cartItem
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItemDTO cartItemDTO : orderDTO.getCartItems()){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            Long productId = cartItemDTO.getProductId();
            Integer numberOfProduct = cartItemDTO.getQuantity();
            Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new DataNotFoundException(String.format("Product not found with id= %d",productId)));

            orderDetail.setProduct(existingProduct);
            orderDetail.setNumberOfProducts(numberOfProduct);
            orderDetail.setPrice(existingProduct.getPrice());
            orderDetails.add(orderDetail);
        }
        order.setOrderDetails(orderDetails);
        orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);
        List<OrderDetail> orderDetails1= order.getOrderDetails();
        OrderResponse orderResponse = OrderResponse.fromOrder(order);
        return orderResponse;
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        try{
            Order order = orderRepository.findById(id).orElseThrow(()-> new DataNotFoundException(String.format("order with id = %d not found", id)));
            OrderResponse orderResponse = OrderResponse.fromOrder(order);
            return orderResponse;
        }catch (Exception e){
            return null;
        }

    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {

            Order order = orderRepository.findById(id).orElseThrow(()-> new DataNotFoundException(String.format("order with id = %d not found", id)));
            User existingUser = userRepository.findById(orderDTO.getUserId()).orElseThrow(()-> new DataNotFoundException(String.format("user with user's id = %d not found", orderDTO.getUserId())));
            modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));
            modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
            modelMapper.map(orderDTO, order);
            order.setUser(existingUser);
            order.setStatus(order.getStatus());
            orderRepository.save(order);
            OrderResponse orderResponse = OrderResponse.fromOrder(order);
            return orderResponse;

    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        // Không xóa cứng chỉ xóa mềm
        if(order != null){
            order.setActive(false);
            orderRepository.save(order);
        }
        return;
    }

    @Override
    public List<OrderResponse> getOrderByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(userId);
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order data : orders){
            OrderResponse orderResponse = OrderResponse.fromOrder(data);
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }
    @Override
    public Page<OrderResponse> getOrdersByKeyword(String keyword, PageRequest pageRequest) {
        Page<Order> orderPages = orderRepository.finKeyword(keyword, pageRequest);
        return orderPages.map(OrderResponse::fromOrder);
    }
}
