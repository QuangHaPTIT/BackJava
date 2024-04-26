package com.example.ShopApp.repositories;

import com.example.ShopApp.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
    @Query(value = "SELECT COUNT(product_id) FROM PRODUCT_IMAGES WHERE PRODUCT_IMAGES.product_id = ?1", nativeQuery = true)
    Long countByProductId(Long id);
}
