package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.category.SubCategory;
import com.example.expensetracker.category.SubCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

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
    public ExpenseDtos.ExpensePageResponse listExpenses(LocalDate startDate, LocalDate endDate, int page, int size) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate cannot be after endDate");
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater");
        }
        if (size <= 0 || size > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 200");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate", "id"));
        Page<Expense> expensePage;
        if (startDate != null && endDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateBetween(startDate, endDate, pageable);
        } else if (startDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateGreaterThanEqual(startDate, pageable);
        } else if (endDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateLessThanEqual(endDate, pageable);
        } else {
            expensePage = expenseRepository.findAll(pageable);
        }

        return new ExpenseDtos.ExpensePageResponse(
                expensePage.stream().map(this::toResponse).toList(),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages()
        );
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
