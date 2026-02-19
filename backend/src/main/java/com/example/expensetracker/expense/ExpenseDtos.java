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
            BigDecimal last30DaysTotal,
            BigDecimal lastMonthTotal,
            BigDecimal lastQuarterTotal,
            BigDecimal lastYearTotal,
            PeriodSummaryPoint currentMonthSummary,
            PeriodSummaryPoint samePeriodLastMonthSummary,
            PeriodSummaryPoint last30DaysSummary,
            PeriodSummaryPoint lastMonthSummary,
            PeriodSummaryPoint lastQuarterSummary,
            PeriodSummaryPoint lastYearSummary,
            List<MonthlyTotalPoint> monthlyTotals,
            List<MonthlyIncomeExpensePoint> monthlyIncomeExpensePoints,
            List<MonthlySavingRatePoint> monthlySavingRatePoints,
            List<CategoryTotalPoint> currentMonthCategoryTotals,
            List<CategoryYearTrendPoint> topYearlyCategoryTrends
    ) {
    }

    public record MonthlyTotalPoint(
            String yearMonth,
            BigDecimal total,
            long count
    ) {
    }

    public record MonthlyIncomeExpensePoint(
            String yearMonth,
            BigDecimal incomeTotal,
            BigDecimal expenseTotal,
            BigDecimal netAmount
    ) {
    }

    public record MonthlySavingRatePoint(
            String yearMonth,
            BigDecimal savingAmount,
            BigDecimal incomeTotal,
            BigDecimal savingRatePercent
    ) {
    }

    public record CategoryTotalPoint(
            String categoryName,
            BigDecimal total,
            long count
    ) {
    }

    public record CategoryYearTrendPoint(
            String categoryName,
            BigDecimal yearTotal,
            List<MonthlyTotalPoint> monthlyTrend
    ) {
    }

    public record PeriodSummaryPoint(
            BigDecimal expenseTotal,
            BigDecimal incomeTotal,
            BigDecimal netAmount
    ) {
    }

    public record CreateExpenseRequest(
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotBlank @Size(max = 300) String description,
            @NotNull LocalDate expenseDate,
            TransactionType type,
            @NotNull Long categoryId,
            @NotNull Long subCategoryId
    ) {
    }
}
