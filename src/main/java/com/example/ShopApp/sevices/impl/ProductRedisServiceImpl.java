package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.response.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductRedisServiceImpl {
    // clear cached data in redis
    void clear();
    List<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException;
    void saveAllProducts(List<ProductResponse> productResponses, String keyword,
                         Long categoryId,
                         PageRequest pageRequest) throws JsonProcessingException;
}
