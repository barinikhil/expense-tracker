package com.example.expensetracker.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
    Page<Expense> findAllByTransactionType(TransactionType type, Pageable pageable);
    Page<Expense> findAllByExpenseDateBetweenAndTransactionType(LocalDate startDate, LocalDate endDate, TransactionType type, Pageable pageable);
    Page<Expense> findAllByExpenseDateGreaterThanEqualAndTransactionType(LocalDate startDate, TransactionType type, Pageable pageable);
    Page<Expense> findAllByExpenseDateLessThanEqualAndTransactionType(LocalDate endDate, TransactionType type, Pageable pageable);
    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.createdBy = :createdBy
              AND e.transactionType = :type
              AND (:startDate IS NULL OR e.expenseDate >= :startDate)
              AND (:endDate IS NULL OR e.expenseDate <= :endDate)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:subCategoryId IS NULL OR e.subCategory.id = :subCategoryId)
              AND (:minAmount IS NULL OR e.amount >= :minAmount)
              AND (:maxAmount IS NULL OR e.amount <= :maxAmount)
            """)
    Page<Expense> findTransactionsWithFilters(
            @Param("createdBy") String createdBy,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            Pageable pageable
    );
    List<Expense> findAllByExpenseDateBetweenAndTransactionTypeOrderByExpenseDateDescIdDesc(LocalDate startDate, LocalDate endDate, TransactionType type);
    long countByExpenseDateBetween(LocalDate startDate, LocalDate endDate);
    long countByExpenseDateBetweenAndCategory_Id(LocalDate startDate, LocalDate endDate, Long categoryId);
    boolean existsByDescriptionStartingWith(String prefix);
    long countByDescriptionStartingWithAndExpenseDateBetween(String prefix, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.budget.id = :budgetId
              AND e.createdBy = :createdBy
              AND e.transactionType = :type
              AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByBudgetAndTypeAndDateRange(
            @Param("budgetId") Long budgetId,
            @Param("createdBy") String createdBy,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.createdBy = :createdBy
              AND (:startDate IS NULL OR e.expenseDate >= :startDate)
              AND (:endDate IS NULL OR e.expenseDate <= :endDate)
            """)
    Page<Expense> findExpensesWithDateFilters(
            @Param("createdBy") String createdBy,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.createdBy = :createdBy
              AND e.expenseDate BETWEEN :startDate AND :endDate
            ORDER BY e.expenseDate DESC, e.id DESC
            """)
    List<Expense> findAllByCreatedByAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
            @Param("createdBy") String createdBy,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    java.util.Optional<Expense> findByIdAndCreatedByIgnoreCase(Long id, String createdBy);
}
