package com.example.expensetracker.expense;

import com.example.expensetracker.budget.BudgetRepository;
import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.category.CategoryType;
import com.example.expensetracker.category.SubCategory;
import com.example.expensetracker.category.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u001", null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listTransactions_shouldApplyAmountSortAscending() {
        Pageable expectedPageable = PageRequest.of(0, 10);
        when(expenseRepository.findTransactionsWithFilters(
                eq("u001"),
                eq(TransactionType.EXPENSE),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

        expenseService.listTransactions(
                null,
                null,
                null,
                null,
                null,
                null,
                TransactionType.EXPENSE,
                "amount",
                "asc",
                0,
                10
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(expenseRepository).findTransactionsWithFilters(
                eq("u001"),
                eq(TransactionType.EXPENSE),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                pageableCaptor.capture()
        );

        Sort sort = pageableCaptor.getValue().getSort();
        Sort.Order amountOrder = sort.getOrderFor("amount");
        Sort.Order dateOrder = sort.getOrderFor("expenseDate");
        Sort.Order idOrder = sort.getOrderFor("id");

        assertThat(amountOrder).isNotNull();
        assertThat(amountOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(dateOrder).isNotNull();
        assertThat(dateOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(idOrder).isNotNull();
        assertThat(idOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listTransactions_shouldRejectInvalidSortBy() {
        assertThatThrownBy(() -> expenseService.listTransactions(
                null,
                null,
                null,
                null,
                null,
                null,
                TransactionType.EXPENSE,
                "invalid",
                "desc",
                0,
                10
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException exception = (ResponseStatusException) ex;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).contains("sortBy must be one of");
                });

        verify(expenseRepository, never()).findTransactionsWithFilters(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void updateExpense_shouldRejectTransactionTypeChange() {
        Category expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setType(CategoryType.EXPENSE);

        Category incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setType(CategoryType.INCOME);

        SubCategory incomeSubCategory = new SubCategory();
        incomeSubCategory.setId(20L);
        incomeSubCategory.setCategory(incomeCategory);

        Expense existingExpense = new Expense();
        existingExpense.setId(100L);
        existingExpense.setTransactionType(TransactionType.EXPENSE);
        existingExpense.setCategory(expenseCategory);
        existingExpense.setSubCategory(incomeSubCategory);
        existingExpense.setAmount(new BigDecimal("50.00"));
        existingExpense.setDescription("Original");
        existingExpense.setExpenseDate(LocalDate.now());

        ExpenseDtos.CreateExpenseRequest request = new ExpenseDtos.CreateExpenseRequest(
                new BigDecimal("100.00"),
                "Salary",
                LocalDate.now(),
                TransactionType.INCOME,
                incomeCategory.getId(),
                incomeSubCategory.getId(),
                null
        );

        when(expenseRepository.findByIdAndCreatedByIgnoreCase(100L, "u001")).thenReturn(Optional.of(existingExpense));
        when(categoryRepository.findByIdAndCreatedByIgnoreCase(incomeCategory.getId(), "u001")).thenReturn(Optional.of(incomeCategory));
        when(subCategoryRepository.findByIdAndCreatedByIgnoreCase(incomeSubCategory.getId(), "u001")).thenReturn(Optional.of(incomeSubCategory));

        assertThatThrownBy(() -> expenseService.updateExpense(100L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException exception = (ResponseStatusException) ex;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).isEqualTo("Changing transaction type is not allowed for updates");
                });

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteTransaction_shouldDeleteWhenIdExists() {
        Expense existingExpense = new Expense();
        existingExpense.setId(42L);
        when(expenseRepository.findByIdAndCreatedByIgnoreCase(42L, "u001")).thenReturn(Optional.of(existingExpense));

        expenseService.deleteTransaction(42L);

        verify(expenseRepository, times(1)).delete(existingExpense);
    }

    @Test
    void deleteTransaction_shouldReturnNotFoundWhenMissing() {
        when(expenseRepository.findByIdAndCreatedByIgnoreCase(404L, "u001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteTransaction(404L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException exception = (ResponseStatusException) ex;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getReason()).isEqualTo("Transaction not found");
                });

        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
