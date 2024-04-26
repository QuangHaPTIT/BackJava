package com.example.ShopApp.repositories;

import com.example.ShopApp.entity.Category;
import com.example.ShopApp.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
