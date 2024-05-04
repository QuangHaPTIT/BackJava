package com.example.ShopApp.controllers;

import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.dtos.OrderDTO;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.BaseResponse;
import com.example.ShopApp.response.OrderListResponse;
import com.example.ShopApp.response.OrderResponse;
import com.example.ShopApp.sevices.impl.OrderServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OrderController {

    private final OrderServiceImpl orderService;
    private final LocalizationUtils localizationUtils;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO orderDTO, BindingResult result){
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(errorMessages);
            }
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok().body(orderResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getOrders(@Valid @PathVariable("user_id") Long userId){
        try{
            List<OrderResponse> orderResponses = orderService.getOrderByUserId(userId);
            return ResponseEntity.ok().body(orderResponses);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@Valid @PathVariable("id") Long id){
        try{
            OrderResponse orderResponse = orderService.getOrderById(id);
            return ResponseEntity.ok().body(orderResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrder(@Valid @PathVariable Long id, @Valid @RequestBody OrderDTO orderDTO) throws DataNotFoundException {
        OrderResponse orderResponse = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok().body(orderResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<String> deleteOrder(@Valid @PathVariable Long id){
        // Xóa mềm => cập nhật trường active = false
        orderService.deleteOrder(id);
        return ResponseEntity.ok().body("Delete is success");
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getOrderByKeyword(@RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "12") int limit){
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
        Page<OrderResponse> orderResponsePage = orderService.getOrdersByKeyword(keyword, pageRequest);
        int totalPage = orderResponsePage.getTotalPages();
        List<OrderResponse> orderResponseList = orderResponsePage.getContent();
        return ResponseEntity.ok().body(
                OrderListResponse
                        .builder()
                        .orderResponses(orderResponseList)
                        .totalPages(totalPage)
                        .build());
    }
}
