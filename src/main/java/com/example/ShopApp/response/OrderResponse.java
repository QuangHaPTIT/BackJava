package com.example.ShopApp.response;

import com.example.ShopApp.entity.Order;
import com.example.ShopApp.entity.OrderDetail;
import com.example.ShopApp.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    @JsonProperty("fullname")
    private String fullName;

    private String email;
    @JsonProperty("phone_number")

    private String phoneNumber;

    private String address;

    private String note;

    @JsonProperty("order_date")
    private Date orderDate;

    private String status;

    @JsonProperty("total_money")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private LocalDate shippingDate;

    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("order_details")
    private List<OrderDetailResponse> orderDetailResponse;
    public static OrderResponse fromOrder(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .email(order.getEmail())
                .note(order.getNote())
                .active(order.isActive())
                .orderDate(order.getOrderDate())
                .fullName(order.getFullName())
                .userId(UserResponse.fromUser((order.getUser())).getId())
//                .userResponse(UserResponse.fromUser(order.getUser()))
                .phoneNumber(order.getPhoneNumber())
                .address(order.getAddress())
                .status(order.getStatus())
                .totalMoney(order.getTotalMoney())
                .shippingMethod(order.getShippingMethod())
                .shippingAddress(order.getShippingAddress())
                .shippingDate(order.getShippingDate())
                .trackingNumber(order.getTrackingNumber())
                .paymentMethod(order.getPaymentMethod())
                .orderDetailResponse(fromListOrder(order))
                .build();
    }

    private static List<OrderDetailResponse> fromListOrder(Order order){
        List<OrderDetailResponse> orderDetailResponses = new ArrayList<>();
        for(OrderDetail orderDetail : order.getOrderDetails()){
            OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
            orderDetailResponses.add(orderDetailResponse);
        }
        return orderDetailResponses;
    }
}
