import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {
  BackendService,
  DashboardCategoryTotal,
  DashboardCategoryYearTrend,
  DashboardMonthlyTotal,
  DashboardSummaryResponse
} from '../../services/backend.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit {
  readonly topTrendColors = ['#0f766e', '#2563eb', '#9333ea', '#ea580c', '#dc2626'];
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

  get topYearlyCategoryTrends(): DashboardCategoryYearTrend[] {
    return this.summary?.topYearlyCategoryTrends ?? [];
  }

  get maxMonthlyTotal(): number {
    const max = this.monthlyTotals.reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
  }

  get totalCategorySpend(): number {
    return this.categoryTotals.reduce((acc, item) => acc + item.total, 0);
  }

  get maxTopYearlyTrendPointTotal(): number {
    const max = this.topYearlyCategoryTrends
      .flatMap((trend) => trend.monthlyTrend)
      .reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
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

  topTrendLinePoints(monthlyTrend: DashboardMonthlyTotal[]): string {
    const width = 620;
    const height = 220;
    const pad = 20;
    const steps = Math.max(1, monthlyTrend.length - 1);
    return monthlyTrend
      .map((point, index) => {
        const x = pad + (index * (width - pad * 2)) / steps;
        const ratio = point.total / this.maxTopYearlyTrendPointTotal;
        const y = height - pad - ratio * (height - pad * 2);
        return `${x},${Math.round(y)}`;
      })
      .join(' ');
  }

  topTrendColor(index: number): string {
    return this.topTrendColors[index % this.topTrendColors.length];
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
