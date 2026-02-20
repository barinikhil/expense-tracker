package com.example.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByNameIgnoreCase(String name);
    Optional<Budget> findByDefaultBudgetTrue();
    boolean existsByNameIgnoreCase(String name);
}

