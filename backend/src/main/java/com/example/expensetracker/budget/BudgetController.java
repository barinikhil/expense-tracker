package com.example.expensetracker.budget;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/budgets")
    public List<BudgetDtos.BudgetResponse> listBudgets() {
        return budgetService.listBudgets();
    }

    @PostMapping("/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetDtos.BudgetResponse createBudget(@Valid @RequestBody BudgetDtos.CreateBudgetRequest request) {
        return budgetService.createBudget(request);
    }

    @PutMapping("/budgets/{id}")
    public BudgetDtos.BudgetResponse updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDtos.UpdateBudgetRequest request
    ) {
        return budgetService.updateBudget(id, request);
    }
}

