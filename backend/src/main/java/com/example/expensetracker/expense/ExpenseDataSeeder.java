package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.category.SubCategory;
import com.example.expensetracker.category.SubCategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Order(20)
public class ExpenseDataSeeder implements ApplicationRunner {

    private static final String DUMMY_PREFIX = "Dummy expense - ";
    private static final int MIN_RECORDS_PER_MONTH = 25;
    private static final int MAX_RECORDS_PER_MONTH = 50;
    private static final Map<String, AmountRange> AMOUNT_RANGES = Map.of(
            "Food", new AmountRange(8, 80),
            "Shopping", new AmountRange(20, 250),
            "Entertainment", new AmountRange(10, 150)
    );

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ExpenseDataSeeder(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<CategoryBundle> categoryBundles = loadCategoryBundles();
        if (categoryBundles.isEmpty()) {
            return;
        }

        List<Expense> expensesToSave = new ArrayList<>();
        Random random = new Random(20260218L);
        YearMonth currentMonth = YearMonth.now();

        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            YearMonth month = currentMonth.minusMonths(monthOffset);
            int targetCount = MIN_RECORDS_PER_MONTH + random.nextInt(MAX_RECORDS_PER_MONTH - MIN_RECORDS_PER_MONTH + 1);

            long existingDummyCount = expenseRepository.countByDescriptionStartingWithAndExpenseDateBetween(
                    DUMMY_PREFIX,
                    month.atDay(1),
                    month.atEndOfMonth()
            );

            long missingCount = Math.max(0, targetCount - existingDummyCount);
            for (int i = 0; i < missingCount; i++) {
                CategoryBundle bundle = categoryBundles.get(random.nextInt(categoryBundles.size()));
                SubCategory subCategory = bundle.subCategories().get(random.nextInt(bundle.subCategories().size()));
                expensesToSave.add(buildExpense(month, bundle.category(), subCategory, random));
            }
        }

        if (!expensesToSave.isEmpty()) {
            expenseRepository.saveAll(expensesToSave);
        }
    }

    private List<CategoryBundle> loadCategoryBundles() {
        List<CategoryBundle> bundles = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            List<SubCategory> subCategories = subCategoryRepository.findAllByCategory_Id(category.getId());
            if (!subCategories.isEmpty()) {
                bundles.add(new CategoryBundle(category, subCategories));
            }
        }
        return bundles;
    }

    private Expense buildExpense(YearMonth month, Category category, SubCategory subCategory, Random random) {
        Expense expense = new Expense();
        expense.setCategory(category);
        expense.setSubCategory(subCategory);
        expense.setExpenseDate(randomDateInMonth(month, random));
        expense.setAmount(randomAmountFor(category.getName(), random));
        expense.setDescription(DUMMY_PREFIX + subCategory.getName());
        return expense;
    }

    private LocalDate randomDateInMonth(YearMonth month, Random random) {
        int day = 1 + random.nextInt(month.lengthOfMonth());
        return month.atDay(day);
    }

    private BigDecimal randomAmountFor(String categoryName, Random random) {
        AmountRange range = AMOUNT_RANGES.getOrDefault(categoryName, new AmountRange(10, 100));
        double value = range.min() + (range.max() - range.min()) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record CategoryBundle(Category category, List<SubCategory> subCategories) {}
    private record AmountRange(double min, double max) {}
}
