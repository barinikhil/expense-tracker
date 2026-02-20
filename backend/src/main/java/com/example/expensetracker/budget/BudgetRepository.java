package com.example.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByNameIgnoreCase(String name);
    Optional<Budget> findByNameIgnoreCaseAndCreatedByIgnoreCase(String name, String createdBy);
    Optional<Budget> findByDefaultBudgetTrueAndCreatedByIgnoreCase(String createdBy);
    Optional<Budget> findByIdAndCreatedByIgnoreCase(Long id, String createdBy);
    List<Budget> findAllByCreatedByIgnoreCaseOrderByNameAsc(String createdBy);
    boolean existsByNameIgnoreCase(String name);
}
