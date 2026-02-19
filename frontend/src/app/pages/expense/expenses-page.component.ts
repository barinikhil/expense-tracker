import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, Category, Expense, SubCategory, TransactionType } from '../../services/backend.service';

@Component({
  selector: 'app-expenses-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './expenses-page.component.html',
  styleUrl: './expenses-page.component.css'
})
export class ExpensesPageComponent implements OnInit {
  error = '';
  expenses: Expense[] = [];
  categories: Category[] = [];
  transactionType: TransactionType = 'EXPENSE';
  startDate = '';
  endDate = '';
  categoryId: number | null = null;
  subCategoryId: number | null = null;
  minAmount: number | null = null;
  maxAmount: number | null = null;
  sortBy: 'expenseDate' | 'amount' | 'category' | 'subCategory' = 'expenseDate';
  sortDir: 'asc' | 'desc' = 'desc';
  pageSize = 10;
  readonly pageSizeOptions = [10, 20, 50, 100];
  currentPage = 1;
  totalPages = 1;
  totalElements = 0;
  deletingTransactionId: number | null = null;

  constructor(
    private readonly backendService: BackendService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.transactionType = (this.route.snapshot.data['transactionType'] as TransactionType) ?? 'EXPENSE';
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.toInputDate(firstDay);
    this.endDate = this.toInputDate(today);
    this.loadCategories();
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.error = '';
    this.backendService
      .listTransactions(
        this.transactionType,
        this.currentPage - 1,
        this.pageSize,
        this.startDate,
        this.endDate,
        this.categoryId,
        this.subCategoryId,
        this.minAmount,
        this.maxAmount,
        this.sortBy,
        this.sortDir
      )
      .subscribe({
      next: (response) => {
        this.expenses = response.items;
        this.totalPages = Math.max(1, response.totalPages);
        this.totalElements = response.totalElements;
        this.currentPage = response.page + 1;
      },
      error: () => {
        this.error = 'Failed to load expenses.';
      }
    });
  }

  get pageTitle(): string {
    return this.transactionType === 'INCOME' ? 'Income' : 'Expenses';
  }

  get pageSubtitle(): string {
    return this.transactionType === 'INCOME'
      ? 'Track your income entries.'
      : 'Track your spending entries.';
  }

  get addRoute(): string {
    return this.transactionType === 'INCOME' ? '/transactions/income/add' : '/transactions/expense/add';
  }

  editRoute(id: number): string {
    return this.transactionType === 'INCOME'
      ? `/transactions/income/edit/${id}`
      : `/transactions/expense/edit/${id}`;
  }

  get filteredCategories(): Category[] {
    if (this.transactionType === 'INCOME') {
      return this.categories.filter((category) => category.type === 'INCOME');
    }
    return this.categories.filter(
      (category) => category.type === 'EXPENSE' || category.type === 'SAVING'
    );
  }

  get filteredSubCategories(): SubCategory[] {
    if (this.categoryId === null) {
      return [];
    }
    return this.categories.find((category) => category.id === this.categoryId)?.subCategories ?? [];
  }

  resetToCurrentMonth(): void {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.toInputDate(firstDay);
    this.endDate = this.toInputDate(today);
    this.categoryId = null;
    this.subCategoryId = null;
    this.minAmount = null;
    this.maxAmount = null;
    this.currentPage = 1;
    this.loadExpenses();
  }

  onCategoryChange(): void {
    this.subCategoryId = null;
  }

  get pageStartRecord(): number {
    if (!this.totalElements) {
      return 0;
    }
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  get pageEndRecord(): number {
    return Math.min(this.currentPage * this.pageSize, this.totalElements);
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.loadExpenses();
  }

  previousPage(): void {
    if (this.currentPage <= 1) {
      return;
    }
    this.currentPage -= 1;
    this.loadExpenses();
  }

  nextPage(): void {
    if (this.currentPage >= this.totalPages) {
      return;
    }
    this.currentPage += 1;
    this.loadExpenses();
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadExpenses();
  }

  toggleSort(field: 'amount' | 'category' | 'subCategory'): void {
    if (this.sortBy === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDir = 'asc';
    }
    this.currentPage = 1;
    this.loadExpenses();
  }

  sortIndicator(field: 'amount' | 'category' | 'subCategory'): string {
    if (this.sortBy !== field) {
      return '';
    }
    return this.sortDir === 'asc' ? '▲' : '▼';
  }

  deleteTransaction(expense: Expense): void {
    const label = this.transactionType === 'INCOME' ? 'income' : 'expense';
    const confirmed = window.confirm(`Delete this ${label} entry?`);
    if (!confirmed || this.deletingTransactionId !== null) {
      return;
    }

    this.error = '';
    this.deletingTransactionId = expense.id;
    this.backendService.deleteTransaction(expense.id).subscribe({
      next: () => {
        if (this.expenses.length === 1 && this.currentPage > 1) {
          this.currentPage -= 1;
        }
        this.deletingTransactionId = null;
        this.loadExpenses();
      },
      error: () => {
        this.deletingTransactionId = null;
        this.error = `Failed to delete ${label}.`;
      }
    });
  }

  private loadCategories(): void {
    this.backendService.listCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.error = 'Failed to load categories.';
      }
    });
  }

  private toInputDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
