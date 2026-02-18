import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit {
  readonly topTrendColors = ['#0f766e', '#2563eb', '#9333ea', '#ea580c', '#dc2626'];
  readonly topNOptions = [1, 3, 5, 10];
  readonly categoryTopOptions = [3, 5, 10, 100];
  loading = true;
  error = '';
  selectedTopN = 5;
  selectedCategoryTopN = 5;
  hiddenCategoryNames = new Set<string>();
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

  get visibleCategoryTotals(): DashboardCategoryTotal[] {
    return this.categoryTotals.slice(0, this.selectedCategoryTopN);
  }

  get topYearlyCategoryTrends(): DashboardCategoryYearTrend[] {
    return this.summary?.topYearlyCategoryTrends ?? [];
  }

  get visibleTopYearlyCategoryTrends(): DashboardCategoryYearTrend[] {
    return this.topYearlyCategoryTrends.filter((trend) => !this.hiddenCategoryNames.has(trend.categoryName));
  }

  get maxMonthlyTotal(): number {
    const max = this.monthlyTotals.reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
  }

  get totalCategorySpend(): number {
    return this.categoryTotals.reduce((acc, item) => acc + item.total, 0);
  }

  get maxTopYearlyTrendPointTotal(): number {
    const max = this.visibleTopYearlyCategoryTrends
      .flatMap((trend) => trend.monthlyTrend)
      .reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
  }

  get topTrendMonths(): DashboardMonthlyTotal[] {
    return this.visibleTopYearlyCategoryTrends[0]?.monthlyTrend ?? this.topYearlyCategoryTrends[0]?.monthlyTrend ?? [];
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
    return monthlyTrend
      .map((point, index) => {
        const x = this.topTrendPointX(index, monthlyTrend.length);
        const y = this.topTrendPointY(point.total);
        return `${x},${Math.round(y)}`;
      })
      .join(' ');
  }

  topTrendPointX(pointIndex: number, pointCount: number): number {
    return 20 + (pointIndex * (580 / (Math.max(1, pointCount - 1))));
  }

  topTrendPointY(total: number): number {
    return 200 - ((total / this.maxTopYearlyTrendPointTotal) * 180);
  }

  topTrendColor(index: number): string {
    return this.topTrendColors[index % this.topTrendColors.length];
  }

  trendColor(categoryName: string): string {
    const index = this.topYearlyCategoryTrends.findIndex((trend) => trend.categoryName === categoryName);
    return this.topTrendColor(Math.max(0, index));
  }

  isCategoryHidden(categoryName: string): boolean {
    return this.hiddenCategoryNames.has(categoryName);
  }

  toggleCategory(categoryName: string): void {
    if (this.hiddenCategoryNames.has(categoryName)) {
      this.hiddenCategoryNames.delete(categoryName);
      return;
    }
    this.hiddenCategoryNames.add(categoryName);
  }

  showAllCategories(): void {
    this.hiddenCategoryNames.clear();
  }

  hideAllCategories(): void {
    this.hiddenCategoryNames = new Set(this.topYearlyCategoryTrends.map((trend) => trend.categoryName));
  }

  onTopNChange(): void {
    this.hiddenCategoryNames.clear();
    this.loadSummary();
  }

  private loadSummary(): void {
    this.loading = true;
    this.error = '';
    this.backendService.getDashboardSummary(this.selectedTopN).subscribe({
      next: (summary) => {
        this.summary = summary;
        const latestNames = new Set(summary.topYearlyCategoryTrends.map((item) => item.categoryName));
        this.hiddenCategoryNames.forEach((name) => {
          if (!latestNames.has(name)) {
            this.hiddenCategoryNames.delete(name);
          }
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
      }
    });
  }
}
