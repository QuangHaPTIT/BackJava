package com.example.ShopApp.controllers;

import com.example.ShopApp.response.CategoryResponse;
import com.example.ShopApp.sevices.CategorySevice;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
@AllArgsConstructor

public class HealthCheckController {

    private final CategorySevice categorySevice;
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try{
            List<CategoryResponse> categoryResponses = categorySevice.getAllCategories();
            return ResponseEntity.ok("ok");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("failed");
        }
    }

}
