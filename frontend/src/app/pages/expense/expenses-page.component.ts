import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, Category, Expense, SubCategory } from '../../services/backend.service';

@Component({
  selector: 'app-expenses-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './expenses-page.component.html',
  styleUrl: './expenses-page.component.css'
})
export class ExpensesPageComponent implements OnInit {
  error = '';
  categories: Category[] = [];
  expenses: Expense[] = [];

  newExpense: {
    amount: number | null;
    description: string;
    expenseDate: string;
    categoryId: number | null;
    subCategoryId: number | null;
  } = {
      amount: null,
      description: '',
      expenseDate: '',
      categoryId: null,
      subCategoryId: null
    };

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.error = '';

    this.backendService.listCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.error = 'Failed to load categories.';
      }
    });

    this.backendService.listExpenses().subscribe({
      next: (expenses) => {
        this.expenses = expenses;
      },
      error: () => {
        this.error = 'Failed to load expenses.';
      }
    });
  }

  onCategoryChange(): void {
    this.newExpense.subCategoryId = null;
  }

  addExpense(): void {
    if (
      this.newExpense.amount === null ||
      this.newExpense.amount <= 0 ||
      !this.newExpense.description.trim() ||
      !this.newExpense.expenseDate ||
      this.newExpense.categoryId === null ||
      this.newExpense.subCategoryId === null
    ) {
      return;
    }

    const payload = {
      amount: this.newExpense.amount,
      description: this.newExpense.description.trim(),
      expenseDate: this.newExpense.expenseDate,
      categoryId: this.newExpense.categoryId,
      subCategoryId: this.newExpense.subCategoryId
    };

    this.backendService.addExpense(payload).subscribe({
      next: () => {
        this.newExpense = {
          amount: null,
          description: '',
          expenseDate: '',
          categoryId: null,
          subCategoryId: null
        };
        this.loadData();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to add expense.';
      }
    });
  }

  get availableSubCategories(): SubCategory[] {
    if (this.newExpense.categoryId === null) {
      return [];
    }

    return this.categories.find((category) => category.id === this.newExpense.categoryId)?.subCategories ?? [];
  }
}
