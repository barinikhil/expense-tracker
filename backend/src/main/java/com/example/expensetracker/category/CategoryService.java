package com.example.expensetracker.category;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String username = currentUsername();
        return categoryRepository.findAllByCreatedByIgnoreCaseOrderByNameAsc(username).stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryDtos.CategoryResponse createCategory(CategoryDtos.CreateCategoryRequest request) {
        String username = currentUsername();
        categoryRepository.findByNameIgnoreCaseAndCreatedByIgnoreCase(request.name(), username)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                });

        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(request.description().trim());
        category.setType(request.type() == null ? CategoryType.EXPENSE : request.type());

        return toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryDtos.CategoryResponse updateCategory(Long id, CategoryDtos.UpdateCategoryRequest request) {
        String username = currentUsername();
        Category category = categoryRepository.findByIdAndCreatedByIgnoreCase(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.findByNameIgnoreCaseAndCreatedByIgnoreCase(request.name(), username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                });

        category.setName(request.name().trim());
        category.setDescription(request.description().trim());
        category.setType(request.type() == null ? CategoryType.EXPENSE : request.type());

        return toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.SubCategoryResponse> listSubCategories() {
        String username = currentUsername();
        return subCategoryRepository.findAllByCreatedByIgnoreCaseOrderByNameAsc(username).stream()
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
        String username = currentUsername();
        SubCategory subCategory = subCategoryRepository.findByIdAndCreatedByIgnoreCase(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-category not found"));

        Category category = findCategory(request.categoryId());

        subCategory.setName(request.name().trim());
        subCategory.setCategory(category);

        return toSubCategoryResponse(subCategory);
    }

    private Category findCategory(Long categoryId) {
        String username = currentUsername();
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }

        return categoryRepository.findByIdAndCreatedByIgnoreCase(categoryId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return username;
    }

    private CategoryDtos.CategoryResponse toCategoryResponse(Category category) {
        List<CategoryDtos.SubCategoryResponse> subCategories = category.getSubCategories().stream()
                .sorted(Comparator.comparing(SubCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSubCategoryResponse)
                .toList();

        return new CategoryDtos.CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getType(),
                subCategories
        );
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
