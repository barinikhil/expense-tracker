package com.example.expensetracker.category;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public CategoryService(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryDtos.CategoryResponse createCategory(CategoryDtos.CreateCategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.name())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                });

        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(request.description().trim());

        return toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryDtos.CategoryResponse updateCategory(Long id, CategoryDtos.UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                });

        category.setName(request.name().trim());
        category.setDescription(request.description().trim());

        return toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.SubCategoryResponse> listSubCategories() {
        return subCategoryRepository.findAll().stream()
                .sorted(Comparator.comparing(SubCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSubCategoryResponse)
                .toList();
    }

    public CategoryDtos.SubCategoryResponse createSubCategory(CategoryDtos.CreateSubCategoryRequest request) {
        Category category = findCategory(request.categoryId());

        SubCategory subCategory = new SubCategory();
        subCategory.setName(request.name().trim());
        subCategory.setCategory(category);

        return toSubCategoryResponse(subCategoryRepository.save(subCategory));
    }

    public CategoryDtos.SubCategoryResponse updateSubCategory(Long id, CategoryDtos.UpdateSubCategoryRequest request) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-category not found"));

        Category category = findCategory(request.categoryId());

        subCategory.setName(request.name().trim());
        subCategory.setCategory(category);

        return toSubCategoryResponse(subCategory);
    }

    private Category findCategory(Long categoryId) {
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private CategoryDtos.CategoryResponse toCategoryResponse(Category category) {
        List<CategoryDtos.SubCategoryResponse> subCategories = category.getSubCategories().stream()
                .sorted(Comparator.comparing(SubCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSubCategoryResponse)
                .toList();

        return new CategoryDtos.CategoryResponse(category.getId(), category.getName(), category.getDescription(), subCategories);
    }

    private CategoryDtos.SubCategoryResponse toSubCategoryResponse(SubCategory subCategory) {
        return new CategoryDtos.SubCategoryResponse(
                subCategory.getId(),
                subCategory.getName(),
                subCategory.getCategory().getId(),
                subCategory.getCategory().getName()
        );
    }
}
