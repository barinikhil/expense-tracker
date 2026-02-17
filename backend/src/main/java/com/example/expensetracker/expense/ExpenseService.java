package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.category.SubCategory;
import com.example.expensetracker.category.SubCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseDtos.ExpenseResponse> listExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseDtos.ExpenseResponse createExpense(ExpenseDtos.CreateExpenseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        SubCategory subCategory = subCategoryRepository.findById(request.subCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-category not found"));

        if (!subCategory.getCategory().getId().equals(category.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sub-category does not belong to selected category"
            );
        }

        Expense expense = new Expense();
        expense.setAmount(request.amount());
        expense.setDescription(request.description().trim());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(category);
        expense.setSubCategory(subCategory);

        return toResponse(expenseRepository.save(expense));
    }

    private ExpenseDtos.ExpenseResponse toResponse(Expense expense) {
        return new ExpenseDtos.ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getSubCategory().getId(),
                expense.getSubCategory().getName()
        );
    }
}
