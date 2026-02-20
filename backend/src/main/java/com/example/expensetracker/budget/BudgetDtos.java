package com.example.expensetracker.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class BudgetDtos {

    private BudgetDtos() {
    }

    public record BudgetResponse(
            Long id,
            String name,
            BigDecimal amount,
            BudgetPeriod period,
            boolean defaultBudget
    ) {
    }

    public record CreateBudgetRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotNull BudgetPeriod period
    ) {
    }

    public record UpdateBudgetRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotNull BudgetPeriod period
    ) {
    }
}

