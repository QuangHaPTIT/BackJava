package com.example.ShopApp.controllers;

import com.example.ShopApp.dtos.CategoryDTO;
import com.example.ShopApp.entity.Category;
import com.example.ShopApp.response.BaseResponse;
import com.example.ShopApp.response.CategoryResponse;
import com.example.ShopApp.sevices.impl.CategorySeviceImpl;
import com.example.ShopApp.components.LocalizationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
//@Validated
public class CategoryController {
    private final CategorySeviceImpl categorySeviceimpl;
    private final LocalizationUtils localizationUtils;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO, BindingResult result){
        if(result.hasErrors()){
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        categorySeviceimpl.createCategory(categoryDTO);
        BaseResponse baseResponse = BaseResponse.builder()
                .message("Create category successfully")
                .data(categoryDTO)
                .build();
        return ResponseEntity.ok(categoryDTO.getName());
    }

    @GetMapping("")
    public ResponseEntity<?> getAllCategories(){
        List<CategoryResponse> categoryResponses = categorySeviceimpl.getAllCategories();
        BaseResponse baseResponse = BaseResponse.builder()
                .data(categoryResponses)
                .build();
        return ResponseEntity.ok(baseResponse);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO){
        CategoryResponse categoryResponse = categorySeviceimpl.updateCategory(id, categoryDTO);
        BaseResponse baseResponse = BaseResponse.builder()
                .message(localizationUtils.getLocalizedMessage("category.update_category.update_successfully"))
                .data(categoryResponse)
                .build();
        return ResponseEntity.ok(baseResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id){
        categorySeviceimpl.deleteCategory(id);
        BaseResponse baseResponse = BaseResponse.builder()
                .message("Delete category successfully")
                .build();
        return ResponseEntity.ok(baseResponse);
    }
}
