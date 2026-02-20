import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { BackendService, Budget, BudgetPeriod } from '../../services/backend.service';

@Component({
  selector: 'app-budgets-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule
  ],
  templateUrl: './budgets-page.component.html',
  styleUrl: './budgets-page.component.css'
})
export class BudgetsPageComponent implements OnInit {
  error = '';
  budgets: Budget[] = [];
  readonly budgetPeriods: BudgetPeriod[] = ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'];

  newBudget = { name: '', amount: null as number | null, period: 'MONTHLY' as BudgetPeriod };
  editingBudgetId: number | null = null;
  editingBudget = { name: '', amount: null as number | null, period: 'MONTHLY' as BudgetPeriod };

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadBudgets();
  }

  loadBudgets(): void {
    this.error = '';
    this.backendService.listBudgets().subscribe({
      next: (budgets) => {
        this.budgets = budgets;
      },
      error: () => {
        this.error = 'Failed to load budgets.';
      }
    });
  }

  addBudget(): void {
    if (!this.newBudget.name.trim() || this.newBudget.amount === null || this.newBudget.amount <= 0) {
      return;
    }

    this.backendService.addBudget({
      name: this.newBudget.name.trim(),
      amount: this.newBudget.amount,
      period: this.newBudget.period
    }).subscribe({
      next: () => {
        this.newBudget = { name: '', amount: null, period: 'MONTHLY' };
        this.loadBudgets();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to add budget.';
      }
    });
  }

  startEditBudget(budget: Budget): void {
    this.editingBudgetId = budget.id;
    this.editingBudget = { name: budget.name, amount: budget.amount, period: budget.period };
  }

  saveBudgetEdit(): void {
    if (this.editingBudgetId === null || !this.editingBudget.name.trim() || this.editingBudget.amount === null || this.editingBudget.amount <= 0) {
      this.error = 'Budget name and amount are required.';
      return;
    }
    this.backendService.updateBudget(this.editingBudgetId, {
      name: this.editingBudget.name.trim(),
      amount: this.editingBudget.amount,
      period: this.editingBudget.period
    }).subscribe({
      next: () => {
        this.cancelBudgetEdit();
        this.loadBudgets();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to update budget.';
      }
    });
  }

  cancelBudgetEdit(): void {
    this.editingBudgetId = null;
    this.editingBudget = { name: '', amount: null, period: 'MONTHLY' };
  }
}

