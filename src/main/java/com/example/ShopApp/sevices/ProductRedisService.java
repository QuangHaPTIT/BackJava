package com.example.ShopApp.sevices;

import com.example.ShopApp.response.ProductResponse;
import com.example.ShopApp.sevices.impl.ProductRedisServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor

public class ProductRedisService implements ProductRedisServiceImpl {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    private String getKeyFrom(String keyword, Long categoryId, PageRequest pageRequest) {
        int pageNumber  = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        Sort sort = pageRequest.getSort();
        String sortDirection = sort.getOrderFor("id").getDirection() == Sort.Direction.ASC ? "asc" : "desc";
        String key = String.format("all_products:%d:%d:%s:%d:%s", pageNumber, pageSize, sortDirection, categoryId, keyword);
        return key;
        /*
        {
            "all_products:1:10:asc": "list of products object"
        }
        * */
    }
    // clear cache in productRedis
    @Override
    public void clear(){
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) this.redisTemplate.getKeySerializer();
        DefaultStringRedisConnection defaultStringRedisConnection = new DefaultStringRedisConnection(redisConnection, redisSerializer);
        defaultStringRedisConnection.flushAll();
    }

    @Override
    public List<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId,
                                                PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = (String) redisTemplate.opsForValue().get(key);
        List<ProductResponse> productResponses = json != null? redisObjectMapper.readValue(json, new TypeReference<List<ProductResponse>>(){}) : null;
        return productResponses;
    }

    @Override
    public void saveAllProducts(List<ProductResponse> productResponses,
                                String keyword,
                                Long categoryId,
                                PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = redisObjectMapper.writeValueAsString(productResponses);
        redisTemplate.opsForValue().set(key, json);

    }
}
