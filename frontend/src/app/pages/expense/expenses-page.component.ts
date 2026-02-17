import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, Expense } from '../../services/backend.service';

@Component({
  selector: 'app-expenses-page',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './expenses-page.component.html',
  styleUrl: './expenses-page.component.css'
})
export class ExpensesPageComponent implements OnInit {
  error = '';
  expenses: Expense[] = [];

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.error = '';
    this.backendService.listExpenses().subscribe({
      next: (expenses) => {
        this.expenses = expenses;
      },
      error: () => {
        this.error = 'Failed to load expenses.';
      }
    });
  }
}
