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
import java.util.Optional;
import java.util.Random;

@Component
@Order(20)
public class ExpenseDataSeeder implements ApplicationRunner {

    private static final String DUMMY_PREFIX = "Dummy expense - ";
    private static final int MIN_CURRENT_MONTH_RECORDS = 50;
    private static final List<String> TARGET_CATEGORIES = List.of("Food", "Shopping", "Entertainment");
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
        Map<String, CategoryBundle> categoryBundles = loadCategoryBundles();
        if (categoryBundles.size() != TARGET_CATEGORIES.size()) {
            return;
        }

        List<Expense> expensesToSave = new ArrayList<>();
        Random random = new Random(20260218L);
        YearMonth currentMonth = YearMonth.now();
        boolean hasAnyDummyData = expenseRepository.existsByDescriptionStartingWith(DUMMY_PREFIX);

        if (!hasAnyDummyData) {
            for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
                YearMonth month = currentMonth.minusMonths(monthOffset);
                int recordCount = 10 + random.nextInt(6);
                for (int i = 0; i < recordCount; i++) {
                    String categoryName = TARGET_CATEGORIES.get(random.nextInt(TARGET_CATEGORIES.size()));
                    CategoryBundle bundle = categoryBundles.get(categoryName);
                    if (bundle == null || bundle.subCategories().isEmpty()) {
                        continue;
                    }
                    SubCategory subCategory = bundle.subCategories().get(random.nextInt(bundle.subCategories().size()));
                    expensesToSave.add(buildExpense(month, bundle.category(), subCategory, random));
                }
            }
        }

        long currentMonthDummyCount = expenseRepository.countByDescriptionStartingWithAndExpenseDateBetween(
                DUMMY_PREFIX,
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()
        );
        long missingCurrentMonthRecords = Math.max(0, MIN_CURRENT_MONTH_RECORDS - currentMonthDummyCount);
        for (int i = 0; i < missingCurrentMonthRecords; i++) {
            String categoryName = TARGET_CATEGORIES.get(random.nextInt(TARGET_CATEGORIES.size()));
            CategoryBundle bundle = categoryBundles.get(categoryName);
            if (bundle == null || bundle.subCategories().isEmpty()) {
                continue;
            }
            SubCategory subCategory = bundle.subCategories().get(random.nextInt(bundle.subCategories().size()));
            expensesToSave.add(buildExpense(currentMonth, bundle.category(), subCategory, random));
        }

        if (!expensesToSave.isEmpty()) {
            expenseRepository.saveAll(expensesToSave);
        }
    }

    private Map<String, CategoryBundle> loadCategoryBundles() {
        java.util.HashMap<String, CategoryBundle> bundles = new java.util.HashMap<>();
        for (String categoryName : TARGET_CATEGORIES) {
            Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCase(categoryName);
            if (categoryOpt.isEmpty()) {
                continue;
            }
            Category category = categoryOpt.get();
            List<SubCategory> subCategories = subCategoryRepository.findAllByCategory_Id(category.getId());
            if (subCategories.isEmpty()) {
                continue;
            }
            bundles.put(categoryName, new CategoryBundle(category, subCategories));
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
