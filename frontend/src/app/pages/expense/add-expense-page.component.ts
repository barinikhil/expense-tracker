import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { BackendService, Category, SubCategory } from '../../services/backend.service';

@Component({
  selector: 'app-add-expense-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule
  ],
  templateUrl: './add-expense-page.component.html',
  styleUrl: './add-expense-page.component.css'
})
export class AddExpensePageComponent implements OnInit {
  error = '';
  categories: Category[] = [];
  returnHereForAnother = false;

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

  constructor(private readonly backendService: BackendService, private readonly router: Router) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.error = '';

    this.backendService.listCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.error = 'Failed to load categories.';
      }
    });
  }

  onCategoryChange(): void {
    this.newExpense.subCategoryId = null;
  }

  cancel(): void {
    this.router.navigate(['/expenses']);
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
        if (this.returnHereForAnother) {
          this.resetForm();
          this.returnHereForAnother = false;
          return;
        }

        this.router.navigate(['/expenses']);
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

  private resetForm(): void {
    this.newExpense = {
      amount: null,
      description: '',
      expenseDate: '',
      categoryId: null,
      subCategoryId: null
    };
  }
}
