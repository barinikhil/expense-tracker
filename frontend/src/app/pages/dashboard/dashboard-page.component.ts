import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {
  BackendService,
  DashboardCategoryTotal,
  DashboardCategoryYearTrend,
  DashboardMonthlyIncomeExpense,
  DashboardMonthlySavingRate,
  DashboardMonthlyTotal,
  DashboardPeriodSummary,
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

  get monthlyIncomeExpensePoints(): DashboardMonthlyIncomeExpense[] {
    return this.summary?.monthlyIncomeExpensePoints ?? [];
  }

  get monthlySavingRatePoints(): DashboardMonthlySavingRate[] {
    return this.summary?.monthlySavingRatePoints ?? [];
  }

  get currentMonthSummary(): DashboardPeriodSummary {
    return this.summary?.currentMonthSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
  }

  get samePeriodLastMonthSummary(): DashboardPeriodSummary {
    return this.summary?.samePeriodLastMonthSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
  }

  get last30DaysSummary(): DashboardPeriodSummary {
    return this.summary?.last30DaysSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
  }

  get lastMonthSummary(): DashboardPeriodSummary {
    return this.summary?.lastMonthSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
  }

  get lastQuarterSummary(): DashboardPeriodSummary {
    return this.summary?.lastQuarterSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
  }

  get lastYearSummary(): DashboardPeriodSummary {
    return this.summary?.lastYearSummary ?? { expenseTotal: 0, incomeTotal: 0, netAmount: 0, savingAmount: 0, savingRatePercent: 0 };
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

  get maxNetMagnitude(): number {
    const max = this.monthlyIncomeExpensePoints.reduce((acc, item) => Math.max(acc, Math.abs(item.netAmount)), 0);
    return max > 0 ? max : 1;
  }

  get maxTopYearlyTrendPointTotal(): number {
    const max = this.visibleTopYearlyCategoryTrends
      .flatMap((trend) => trend.monthlyTrend)
      .reduce((acc, item) => Math.max(acc, item.total), 0);
    return max > 0 ? max : 1;
  }

  get maxSavingRatePercent(): number {
    const max = this.monthlySavingRatePoints.reduce((acc, item) => Math.max(acc, item.savingRatePercent), 0);
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

  get currentMonthDateRange(): string {
    const today = new Date();
    const start = new Date(today.getFullYear(), today.getMonth(), 1);
    return `${this.formatDate(start)} - ${this.formatDate(today)}`;
  }

  get last30DaysDateRange(): string {
    const today = new Date();
    const start = new Date(today);
    start.setDate(today.getDate() - 29);
    return `${this.formatDate(start)} - ${this.formatDate(today)}`;
  }

  get samePeriodLastMonthDateRange(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth();
    const previousMonthDate = new Date(year, month - 1, 1);
    const previousMonthStart = new Date(previousMonthDate.getFullYear(), previousMonthDate.getMonth(), 1);
    const previousMonthLastDay = new Date(previousMonthDate.getFullYear(), previousMonthDate.getMonth() + 1, 0).getDate();
    const endDay = Math.min(today.getDate(), previousMonthLastDay);
    const previousMonthEnd = new Date(previousMonthDate.getFullYear(), previousMonthDate.getMonth(), endDay);
    return `${this.formatDate(previousMonthStart)} - ${this.formatDate(previousMonthEnd)}`;
  }

  get lastMonthDateRange(): string {
    const today = new Date();
    const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0);
    return `${this.formatDate(lastMonthStart)} - ${this.formatDate(lastMonthEnd)}`;
  }

  get lastQuarterDateRange(): string {
    const today = new Date();
    const lastQuarterStart = new Date(today.getFullYear(), today.getMonth() - 3, 1);
    const lastQuarterEnd = new Date(today.getFullYear(), today.getMonth(), 0);
    return `${this.formatDate(lastQuarterStart)} - ${this.formatDate(lastQuarterEnd)}`;
  }

  get lastYearDateRange(): string {
    const today = new Date();
    const start = new Date(today);
    start.setDate(today.getDate() - 364);
    return `${this.formatDate(start)} - ${this.formatDate(today)}`;
  }

  get currentMonthLabel(): string {
    return `Current Month (${this.currentMonthDateRange})`;
  }

  get last30DaysLabel(): string {
    return `Last 30 Days (${this.last30DaysDateRange})`;
  }

  get samePeriodLastMonthLabel(): string {
    return `Same Period Last Month (${this.samePeriodLastMonthDateRange})`;
  }

  get lastMonthLabel(): string {
    return `Last Month (${this.lastMonthDateRange})`;
  }

  get lastQuarterLabel(): string {
    return `Last Quarter (${this.lastQuarterDateRange})`;
  }

  get lastYearLabel(): string {
    return `Last Year (${this.lastYearDateRange})`;
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

  netBarHeight(netAmount: number): number {
    const ratio = Math.abs(netAmount) / this.maxNetMagnitude;
    return Math.max(6, Math.round(ratio * 84));
  }

  netBarClass(netAmount: number): string {
    return netAmount >= 0 ? 'net-positive' : 'net-negative';
  }

  netAmountClass(netAmount: number): string {
    return netAmount >= 0 ? 'kpi-net-positive' : 'kpi-net-negative';
  }

  savingRatePercent(summary: DashboardPeriodSummary): number {
    return summary.savingRatePercent;
  }

  savingRateHeight(ratePercent: number): number {
    const ratio = ratePercent / this.maxSavingRatePercent;
    return Math.max(6, Math.round(ratio * 150));
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

  private formatDate(value: Date): string {
    return value.toLocaleDateString(undefined, {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }
}
