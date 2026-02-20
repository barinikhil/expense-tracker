package com.example.expensetracker.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    boolean existsByNameIgnoreCaseAndCategory_Id(String name, Long categoryId);
    List<SubCategory> findAllByCategory_Id(Long categoryId);
    List<SubCategory> findAllByCreatedByIgnoreCaseOrderByNameAsc(String createdBy);
    Optional<SubCategory> findByIdAndCreatedByIgnoreCase(Long id, String createdBy);
}
