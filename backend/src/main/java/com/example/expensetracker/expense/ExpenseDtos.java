package com.example.expensetracker.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ExpenseDtos {

    private ExpenseDtos() {
    }

    public record ExpenseResponse(
            Long id,
            BigDecimal amount,
            String description,
            LocalDate expenseDate,
            Long categoryId,
            String categoryName,
            Long subCategoryId,
            String subCategoryName
    ) {
    }

    public record ExpensePageResponse(
            List<ExpenseResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record DashboardSummaryResponse(
            BigDecimal currentMonthTotal,
            long currentMonthCount,
            BigDecimal previousMonthTotal,
            List<MonthlyTotalPoint> monthlyTotals,
            List<CategoryTotalPoint> currentMonthCategoryTotals
    ) {
    }

    public record MonthlyTotalPoint(
            String yearMonth,
            BigDecimal total,
            long count
    ) {
    }

    public record CategoryTotalPoint(
            String categoryName,
            BigDecimal total,
            long count
    ) {
    }

    public record CreateExpenseRequest(
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotBlank @Size(max = 300) String description,
            @NotNull LocalDate expenseDate,
            @NotNull Long categoryId,
            @NotNull Long subCategoryId
    ) {
    }
}
