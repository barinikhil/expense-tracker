package com.example.expensetracker.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findByNameIgnoreCaseAndCreatedByIgnoreCase(String name, String createdBy);
    Optional<Category> findByIdAndCreatedByIgnoreCase(Long id, String createdBy);
    List<Category> findAllByCreatedByIgnoreCaseOrderByNameAsc(String createdBy);
}
