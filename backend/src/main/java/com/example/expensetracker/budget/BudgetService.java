package com.example.expensetracker.budget;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetDtos.BudgetResponse> listBudgets() {
        return budgetRepository.findAll().stream()
                .sorted(Comparator.comparing(Budget::isDefaultBudget).reversed()
                        .thenComparing(Budget::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    public BudgetDtos.BudgetResponse createBudget(BudgetDtos.CreateBudgetRequest request) {
        budgetRepository.findByNameIgnoreCase(request.name().trim())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Budget already exists");
                });

        Budget budget = new Budget();
        budget.setName(request.name().trim());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        budget.setDefaultBudget(false);
        return toResponse(budgetRepository.save(budget));
    }

    public BudgetDtos.BudgetResponse updateBudget(Long id, BudgetDtos.UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        budgetRepository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Budget already exists");
                });

        budget.setName(request.name().trim());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        return toResponse(budget);
    }

    private BudgetDtos.BudgetResponse toResponse(Budget budget) {
        return new BudgetDtos.BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                budget.getPeriod(),
                budget.isDefaultBudget()
        );
    }
}

