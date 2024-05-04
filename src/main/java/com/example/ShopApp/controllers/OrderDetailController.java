package com.example.ShopApp.controllers;


import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.dtos.OrderDetailDTO;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.BaseResponse;
import com.example.ShopApp.response.OrderDetailResponse;
import com.example.ShopApp.sevices.impl.OrderDetailServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController {
    private final OrderDetailServiceImpl orderDetailService;
    private final LocalizationUtils localizationUtils;
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@Valid @PathVariable Long id) throws DataNotFoundException {
        OrderDetailResponse orderDetailResponse = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok().body(orderDetailResponse);
    }

    // Lấy ra danh sách order detail của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@Valid @PathVariable("orderId") Long orderId){
        // Trả về List<?> sau sửa
        List<OrderDetailResponse> orderDetailResponses = orderDetailService.getOrderDetailByOrderId(orderId);
        return ResponseEntity.ok().body(orderDetailResponses);
    }
    // Thêm mới 1 Order Detail
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO, BindingResult result){
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(errorMessages);
            }
            BaseResponse baseResponse = new BaseResponse();
            OrderDetailResponse orderDetailResponse = orderDetailService.createOrderDetail(orderDetailDTO);
            baseResponse.setData(orderDetailResponse);
            baseResponse.setMessage("Create Order Detail Successfully");
            return ResponseEntity.ok().body(baseResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderDetail(@PathVariable Long id, @Valid @RequestBody OrderDetailDTO orderDetailDTO){

        try {
            OrderDetailResponse orderDetailResponse = orderDetailService.updateOrderDetail(id, orderDetailDTO);
            return ResponseEntity.ok().body(orderDetailResponse);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteOrderDetail(@Valid @PathVariable Long id){
        BaseResponse baseResponse = new BaseResponse();
        orderDetailService.deleteOrderDetail(id);
        baseResponse.setMessage("Delete Order Detail Successfully");
        return ResponseEntity.ok().body(baseResponse);
    }

}
