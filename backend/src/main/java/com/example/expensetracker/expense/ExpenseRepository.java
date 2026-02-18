package com.example.expensetracker.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderByExpenseDateDescIdDesc();
    boolean existsByDescriptionStartingWith(String prefix);
    long countByDescriptionStartingWithAndExpenseDateBetween(String prefix, LocalDate startDate, LocalDate endDate);
}
