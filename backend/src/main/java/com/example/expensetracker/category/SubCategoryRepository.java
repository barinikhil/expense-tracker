package com.example.expensetracker.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    boolean existsByNameIgnoreCaseAndCategory_Id(String name, Long categoryId);
    java.util.List<SubCategory> findAllByCategory_Id(Long categoryId);
}
