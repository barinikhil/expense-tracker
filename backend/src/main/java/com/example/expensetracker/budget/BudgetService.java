package com.example.expensetracker.budget;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String username = currentUsername();
        return budgetRepository.findAllByCreatedByIgnoreCaseOrderByNameAsc(username).stream()
                .sorted(Comparator.comparing(Budget::isDefaultBudget).reversed()
                        .thenComparing(Budget::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    public BudgetDtos.BudgetResponse createBudget(BudgetDtos.CreateBudgetRequest request) {
        String username = currentUsername();
        budgetRepository.findByNameIgnoreCaseAndCreatedByIgnoreCase(request.name().trim(), username)
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
        String username = currentUsername();
        Budget budget = budgetRepository.findByIdAndCreatedByIgnoreCase(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        budgetRepository.findByNameIgnoreCaseAndCreatedByIgnoreCase(request.name().trim(), username)
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

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return username;
    }
}
