package com.example.expensetracker.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderByExpenseDateDescIdDesc();
    List<Expense> findAllByExpenseDateBetweenOrderByExpenseDateDescIdDesc(LocalDate startDate, LocalDate endDate);
    List<Expense> findAllByExpenseDateGreaterThanEqualOrderByExpenseDateDescIdDesc(LocalDate startDate);
    List<Expense> findAllByExpenseDateLessThanEqualOrderByExpenseDateDescIdDesc(LocalDate endDate);
    Page<Expense> findAllByExpenseDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Expense> findAllByExpenseDateGreaterThanEqual(LocalDate startDate, Pageable pageable);
    Page<Expense> findAllByExpenseDateLessThanEqual(LocalDate endDate, Pageable pageable);
    long countByExpenseDateBetween(LocalDate startDate, LocalDate endDate);
    long countByExpenseDateBetweenAndCategory_Id(LocalDate startDate, LocalDate endDate, Long categoryId);
    boolean existsByDescriptionStartingWith(String prefix);
    long countByDescriptionStartingWithAndExpenseDateBetween(String prefix, LocalDate startDate, LocalDate endDate);
}
