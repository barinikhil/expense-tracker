package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.category.CategoryType;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional(readOnly = true)
    public ExpenseDtos.ExpensePageResponse listTransactions(
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            int page,
            int size
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate cannot be after endDate");
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater");
        }
        if (size <= 0 || size > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 200");
        }

        TransactionType resolvedType = type == null ? TransactionType.EXPENSE : type;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate", "id"));
        Page<Expense> expensePage;
        if (startDate != null && endDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateBetweenAndTransactionType(
                    startDate,
                    endDate,
                    resolvedType,
                    pageable
            );
        } else if (startDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateGreaterThanEqualAndTransactionType(
                    startDate,
                    resolvedType,
                    pageable
            );
        } else if (endDate != null) {
            expensePage = expenseRepository.findAllByExpenseDateLessThanEqualAndTransactionType(
                    endDate,
                    resolvedType,
                    pageable
            );
        } else {
            expensePage = expenseRepository.findAllByTransactionType(resolvedType, pageable);
        }

        return new ExpenseDtos.ExpensePageResponse(
                expensePage.stream().map(this::toResponse).toList(),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ExpenseDtos.DashboardSummaryResponse getDashboardSummary(int topN) {
        if (topN < 1 || topN > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "topN must be between 1 and 10");
        }

        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(11);
        LocalDate fromDate = startMonth.atDay(1);
        LocalDate toDate = currentMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findAllByExpenseDateBetweenOrderByExpenseDateDescIdDesc(fromDate, toDate);
        Map<YearMonth, List<Expense>> byMonth = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            YearMonth month = startMonth.plusMonths(i);
            byMonth.put(month, new ArrayList<>());
        }
        for (Expense expense : expenses) {
            YearMonth month = YearMonth.from(expense.getExpenseDate());
            List<Expense> monthExpenses = byMonth.get(month);
            if (monthExpenses != null) {
                monthExpenses.add(expense);
            }
        }

        List<ExpenseDtos.MonthlyTotalPoint> monthlyTotals = byMonth.entrySet().stream()
                .map(entry -> {
                    BigDecimal total = sumAmounts(entry.getValue());
                    return new ExpenseDtos.MonthlyTotalPoint(entry.getKey().toString(), total, entry.getValue().size());
                })
                .toList();

        List<ExpenseDtos.MonthlyIncomeExpensePoint> monthlyIncomeExpensePoints = byMonth.entrySet().stream()
                .map(entry -> {
                    BigDecimal incomeTotal = sumAmountsByType(entry.getValue(), TransactionType.INCOME);
                    BigDecimal expenseTotal = sumAmountsByType(entry.getValue(), TransactionType.EXPENSE);
                    return new ExpenseDtos.MonthlyIncomeExpensePoint(
                            entry.getKey().toString(),
                            incomeTotal,
                            expenseTotal,
                            incomeTotal.subtract(expenseTotal).setScale(2, RoundingMode.HALF_UP)
                    );
                })
                .toList();

        List<Expense> currentMonthExpenses = byMonth.getOrDefault(currentMonth, List.of());
        BigDecimal currentMonthTotal = sumAmounts(currentMonthExpenses);

        LocalDate today = LocalDate.now();
        LocalDate last30DaysStart = today.minusDays(29);
        BigDecimal last30DaysTotal = sumAmountsInRange(expenses, last30DaysStart, today);

        YearMonth previousMonth = currentMonth.minusMonths(1);
        BigDecimal lastMonthTotal = sumAmounts(byMonth.getOrDefault(previousMonth, List.of()));

        LocalDate lastQuarterStart = currentMonth.minusMonths(3).atDay(1);
        LocalDate lastQuarterEnd = currentMonth.minusMonths(1).atEndOfMonth();
        BigDecimal lastQuarterTotal = sumAmountsInRange(expenses, lastQuarterStart, lastQuarterEnd);

        LocalDate lastYearStart = today.minusDays(364);
        BigDecimal lastYearTotal = sumAmountsInRange(expenses, lastYearStart, today);

        Map<String, List<Expense>> byCategory = new LinkedHashMap<>();
        for (Expense expense : currentMonthExpenses) {
            if (expense.getTransactionType() != TransactionType.EXPENSE) {
                continue;
            }
            byCategory.computeIfAbsent(expense.getCategory().getName(), key -> new ArrayList<>()).add(expense);
        }

        List<ExpenseDtos.CategoryTotalPoint> categoryTotals = byCategory.entrySet().stream()
                .map(entry -> new ExpenseDtos.CategoryTotalPoint(
                        entry.getKey(),
                        sumAmounts(entry.getValue()),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparing(ExpenseDtos.CategoryTotalPoint::total).reversed())
                .toList();

        Map<String, List<Expense>> byYearCategory = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            if (expense.getTransactionType() != TransactionType.EXPENSE) {
                continue;
            }
            byYearCategory.computeIfAbsent(expense.getCategory().getName(), key -> new ArrayList<>()).add(expense);
        }

        List<ExpenseDtos.CategoryYearTrendPoint> topYearlyCategoryTrends = byYearCategory.entrySet().stream()
                .map(entry -> {
                    BigDecimal yearTotal = sumAmounts(entry.getValue());
                    List<ExpenseDtos.MonthlyTotalPoint> monthlyTrend = byMonth.entrySet().stream()
                            .map(monthEntry -> {
                                List<Expense> categoryMonthExpenses = monthEntry.getValue().stream()
                                        .filter(exp -> exp.getCategory().getName().equalsIgnoreCase(entry.getKey()))
                                        .toList();
                                return new ExpenseDtos.MonthlyTotalPoint(
                                        monthEntry.getKey().toString(),
                                        sumAmounts(categoryMonthExpenses),
                                        categoryMonthExpenses.size()
                                );
                            })
                            .toList();
                    return new ExpenseDtos.CategoryYearTrendPoint(entry.getKey(), yearTotal, monthlyTrend);
                })
                .sorted(Comparator.comparing(ExpenseDtos.CategoryYearTrendPoint::yearTotal).reversed())
                .limit(topN)
                .toList();

        return new ExpenseDtos.DashboardSummaryResponse(
                currentMonthTotal,
                last30DaysTotal,
                lastMonthTotal,
                lastQuarterTotal,
                lastYearTotal,
                monthlyTotals,
                monthlyIncomeExpensePoints,
                categoryTotals,
                topYearlyCategoryTrends
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

        TransactionType resolvedType = request.type() == null ? TransactionType.EXPENSE : request.type();
        if (resolvedType == TransactionType.INCOME && category.getType() != CategoryType.INCOME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must be INCOME for income transactions");
        }
        if (resolvedType == TransactionType.EXPENSE
                && category.getType() != CategoryType.EXPENSE
                && category.getType() != CategoryType.SAVING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must be EXPENSE or SAVING for expense transactions");
        }

        Expense expense = new Expense();
        expense.setAmount(request.amount());
        expense.setDescription(request.description().trim());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(category);
        expense.setSubCategory(subCategory);
        expense.setTransactionType(resolvedType);

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

    private BigDecimal sumAmounts(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumAmountsInRange(List<Expense> expenses, LocalDate startDate, LocalDate endDate) {
        return expenses.stream()
                .filter(expense -> !expense.getExpenseDate().isBefore(startDate) && !expense.getExpenseDate().isAfter(endDate))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumAmountsByType(List<Expense> expenses, TransactionType type) {
        return expenses.stream()
                .filter(expense -> expense.getTransactionType() == type)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
