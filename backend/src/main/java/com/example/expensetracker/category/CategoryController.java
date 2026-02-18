package com.example.expensetracker.category;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryDtos.CategoryResponse> listCategories() {
        return categoryService.listCategories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtos.CategoryResponse createCategory(@Valid @RequestBody CategoryDtos.CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryDtos.CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDtos.UpdateCategoryRequest request
    ) {
        return categoryService.updateCategory(id, request);
    }

    @GetMapping("/sub-categories")
    public List<CategoryDtos.SubCategoryResponse> listSubCategories() {
        return categoryService.listSubCategories();
    }

    @PostMapping("/sub-categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtos.SubCategoryResponse createSubCategory(
            @Valid @RequestBody CategoryDtos.CreateSubCategoryRequest request
    ) {
        return categoryService.createSubCategory(request);
    }

    @PutMapping("/sub-categories/{id}")
    public CategoryDtos.SubCategoryResponse updateSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDtos.UpdateSubCategoryRequest request
    ) {
        return categoryService.updateSubCategory(id, request);
    }
}
