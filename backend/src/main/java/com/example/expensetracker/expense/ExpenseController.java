package com.example.expensetracker.expense;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/expenses")
    public ExpenseDtos.ExpensePageResponse listExpenses(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return expenseService.listExpenses(startDate, endDate, page, size);
    }

    @GetMapping("/dashboard/summary")
    public ExpenseDtos.DashboardSummaryResponse getDashboardSummary(
            @RequestParam(defaultValue = "5") int topN
    ) {
        return expenseService.getDashboardSummary(topN);
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseDtos.ExpenseResponse createExpense(@Valid @RequestBody ExpenseDtos.CreateExpenseRequest request) {
        return expenseService.createExpense(request);
    }
}
