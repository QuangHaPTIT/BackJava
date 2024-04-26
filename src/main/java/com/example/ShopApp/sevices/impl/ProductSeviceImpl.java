package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.dtos.ProductDTO;
import com.example.ShopApp.dtos.ProductImageDTO;
import com.example.ShopApp.entity.Product;
import com.example.ShopApp.entity.ProductImage;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.exceptions.InvalidParamException;
import com.example.ShopApp.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductSeviceImpl {
    public ProductResponse createProduct(ProductDTO productDTO) throws DataNotFoundException;
    public ProductResponse getProductById(Long id) throws DataNotFoundException;
    Page<ProductResponse> getAllProduct(PageRequest pageRequest, String keyword, Long categoryId);
    ProductResponse updateProduct(long id, ProductDTO productDTO) throws DataNotFoundException;
    boolean deleteProduct(long id) throws DataNotFoundException;
    boolean existByName(String name);
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws DataNotFoundException, InvalidParamException;
    public List<ProductResponse> findProductByIds(List<Long> productIds);
}
