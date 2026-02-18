import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, DashboardCategoryTotal, DashboardMonthlyTotal, DashboardSummaryResponse } from '../../services/backend.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit {
  loading = true;
  error = '';
  summary: DashboardSummaryResponse | null = null;

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  get monthlyTotals(): DashboardMonthlyTotal[] {
    return this.summary?.monthlyTotals ?? [];
  }

  get categoryTotals(): DashboardCategoryTotal[] {
    return this.summary?.currentMonthCategoryTotals ?? [];
  }

  get maxMonthlyTotal(): number {
    const max = this.monthlyTotals.reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
  }

  get totalCategorySpend(): number {
    return this.categoryTotals.reduce((acc, item) => acc + item.total, 0);
  }

  monthLabel(yearMonth: string): string {
    const [year, month] = yearMonth.split('-').map((item) => Number(item));
    const date = new Date(year, month - 1, 1);
    return date.toLocaleDateString(undefined, { month: 'short' });
  }

  barHeight(total: number): number {
    const ratio = total / this.maxMonthlyTotal;
    return Math.max(8, Math.round(ratio * 160));
  }

  categoryWidth(total: number): number {
    const totalSpend = this.totalCategorySpend;
    if (!totalSpend) {
      return 0;
    }
    return Math.max(6, Math.round((total / totalSpend) * 100));
  }

  private loadSummary(): void {
    this.loading = true;
    this.error = '';
    this.backendService.getDashboardSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
      }
    });
  }
}
