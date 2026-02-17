package com.example.expensetracker;

import com.example.expensetracker.category.CategoryDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryFeatureIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateListAndUpdateCategoriesAndSubCategories() {
        ResponseEntity<CategoryDtos.CategoryResponse> createdCategory = restTemplate.postForEntity(
                "/api/categories",
                new CategoryDtos.CreateCategoryRequest("Food", "Food-related expenses"),
                CategoryDtos.CategoryResponse.class
        );

        assertThat(createdCategory.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdCategory.getBody()).isNotNull();
        Long categoryId = createdCategory.getBody().id();

        ResponseEntity<CategoryDtos.SubCategoryResponse> createdSubCategory = restTemplate.postForEntity(
                "/api/sub-categories",
                new CategoryDtos.CreateSubCategoryRequest("Groceries", categoryId),
                CategoryDtos.SubCategoryResponse.class
        );

        assertThat(createdSubCategory.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdSubCategory.getBody()).isNotNull();

        restTemplate.put(
                "/api/categories/{id}",
                new CategoryDtos.UpdateCategoryRequest("Food & Dining", "Dining and groceries"),
                categoryId
        );

        ResponseEntity<CategoryDtos.CategoryResponse[]> categories = restTemplate.getForEntity(
                "/api/categories",
                CategoryDtos.CategoryResponse[].class
        );

        assertThat(categories.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(categories.getBody()).isNotNull();
        assertThat(categories.getBody()).hasSize(1);
        assertThat(categories.getBody()[0].name()).isEqualTo("Food & Dining");
        assertThat(categories.getBody()[0].subCategories()).hasSize(1);
    }
}
