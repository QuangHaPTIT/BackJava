package com.example.ShopApp.dtos;

import jakarta.validation.constraints.NotEmpty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    @NotEmpty(message = "Category`name cannot be empty")
    private String name;

}
