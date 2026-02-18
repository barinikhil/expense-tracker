import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, Expense } from '../../services/backend.service';

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
  startDate = '';
  endDate = '';
  pageSize = 10;
  readonly pageSizeOptions = [10, 20, 50, 100];
  currentPage = 1;
  totalPages = 1;
  totalElements = 0;

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.toInputDate(firstDay);
    this.endDate = this.toInputDate(today);
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.error = '';
    this.backendService.listExpenses(this.currentPage - 1, this.pageSize, this.startDate, this.endDate).subscribe({
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

  resetToCurrentMonth(): void {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.toInputDate(firstDay);
    this.endDate = this.toInputDate(today);
    this.currentPage = 1;
    this.loadExpenses();
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

  private toInputDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
