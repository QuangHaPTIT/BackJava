package com.example.ShopApp.sevices;

import com.example.ShopApp.dtos.CategoryDTO;
import com.example.ShopApp.entity.Category;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.repositories.CategoryRepository;
import com.example.ShopApp.response.CategoryResponse;
import com.example.ShopApp.sevices.impl.CategorySeviceImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CategorySevice implements CategorySeviceImpl{
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id).orElseThrow(() ->  new RuntimeException("Category not found"));
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
        return categoryResponse;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
        return categoryResponses;
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryDTO categoryDTO) {

        try{
            Category existCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new DataNotFoundException("Category not found"));
            existCategory.setName(categoryDTO.getName());
            categoryRepository.save(existCategory);
            CategoryResponse categoryResponse = CategoryResponse.builder()
                    .id(existCategory.getId())
                    .name(existCategory.getName())
                    .build();
            return categoryResponse;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
