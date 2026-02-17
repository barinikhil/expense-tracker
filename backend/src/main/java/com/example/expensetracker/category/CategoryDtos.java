package com.example.expensetracker.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record CategoryResponse(Long id, String name, String description, List<SubCategoryResponse> subCategories) {
    }

    public record SubCategoryResponse(Long id, String name, Long categoryId, String categoryName) {
    }

    public record CreateCategoryRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 200) String description
    ) {
    }

    public record UpdateCategoryRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 200) String description
    ) {
    }

    public record CreateSubCategoryRequest(
            @NotBlank @Size(max = 80) String name,
            Long categoryId
    ) {
    }

    public record UpdateSubCategoryRequest(
            @NotBlank @Size(max = 80) String name,
            Long categoryId
    ) {
    }
}
