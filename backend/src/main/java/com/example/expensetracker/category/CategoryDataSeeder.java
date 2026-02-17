package com.example.expensetracker.category;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class CategoryDataSeeder implements ApplicationRunner {

    private static final Map<String, List<String>> DEFAULT_DATA = Map.of(
            "Housing", List.of("Rent", "Maintenance", "Electricity", "Water Bill", "Internet"),
            "Food", List.of("Groceries", "Restaurants", "Snacks", "Coffee"),
            "Transportation", List.of("Fuel", "Public Transport", "Cab / Taxi", "Vehicle Maintenance"),
            "Health", List.of("Doctor Consultation", "Medicines", "Health Insurance", "Medical Tests"),
            "Entertainment", List.of("Movies", "OTT Subscription", "Games", "Travel"),
            "Education", List.of("Course Fees", "Books", "Certifications"),
            "Shopping", List.of("Clothing", "Electronics", "Household Items")
    );

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public CategoryDataSeeder(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        DEFAULT_DATA.forEach((categoryName, subCategoryNames) -> {
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> categoryRepository.save(buildCategory(categoryName)));

            subCategoryNames.forEach(subCategoryName -> {
                boolean exists = subCategoryRepository.existsByNameIgnoreCaseAndCategory_Id(
                        subCategoryName,
                        category.getId()
                );
                if (!exists) {
                    SubCategory subCategory = new SubCategory();
                    subCategory.setName(subCategoryName);
                    subCategory.setCategory(category);
                    subCategoryRepository.save(subCategory);
                }
            });
        });
    }

    private Category buildCategory(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription(categoryName + " expenses");
        return category;
    }
}
