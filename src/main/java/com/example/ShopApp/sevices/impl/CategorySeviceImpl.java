package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.dtos.CategoryDTO;
import com.example.ShopApp.entity.Category;
import com.example.ShopApp.response.CategoryResponse;

import java.util.List;

public interface CategorySeviceImpl {
    Category createCategory(CategoryDTO categoryDTO);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long categoryId, CategoryDTO categoryDTO);
    void deleteCategory(Long id);

}
