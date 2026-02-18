import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

interface HealthResponse {
  status: string;
}

export interface LoginResponse {
  token: string;
  username: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  type: CategoryType;
  subCategories: SubCategory[];
}

export interface SubCategory {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
}

export type TransactionType = 'EXPENSE' | 'INCOME';
export type CategoryType = 'EXPENSE' | 'INCOME' | 'SAVING';

export interface Expense {
  id: number;
  amount: number;
  description: string;
  expenseDate: string;
  categoryId: number;
  categoryName: string;
  subCategoryId: number;
  subCategoryName: string;
}

export interface ExpensePageResponse {
  items: Expense[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DashboardMonthlyTotal {
  yearMonth: string;
  total: number;
  count: number;
}

export interface DashboardCategoryTotal {
  categoryName: string;
  total: number;
  count: number;
}

export interface DashboardCategoryYearTrend {
  categoryName: string;
  yearTotal: number;
  monthlyTrend: DashboardMonthlyTotal[];
}

export interface DashboardSummaryResponse {
  currentMonthTotal: number;
  last30DaysTotal: number;
  lastMonthTotal: number;
  lastQuarterTotal: number;
  lastYearTotal: number;
  monthlyTotals: DashboardMonthlyTotal[];
  currentMonthCategoryTotals: DashboardCategoryTotal[];
  topYearlyCategoryTrends: DashboardCategoryYearTrend[];
}

@Injectable({
  providedIn: 'root'
})
export class BackendService {
//   private readonly apiBaseUrl = 'http://192.168.6.35:9081/api';
  private readonly apiBaseUrl = 'http://192.168.1.18:9081/api';

  constructor(private readonly http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.apiBaseUrl}/health`);
  }

  login(payload: { username: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiBaseUrl}/auth/login`, payload);
  }

  listCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiBaseUrl}/categories`);
  }

  addCategory(payload: { name: string; description: string; type: CategoryType }): Observable<Category> {
    return this.http.post<Category>(`${this.apiBaseUrl}/categories`, payload);
  }

  updateCategory(id: number, payload: { name: string; description: string; type: CategoryType }): Observable<Category> {
    return this.http.put<Category>(`${this.apiBaseUrl}/categories/${id}`, payload);
  }

  listSubCategories(): Observable<SubCategory[]> {
    return this.http.get<SubCategory[]>(`${this.apiBaseUrl}/sub-categories`);
  }

  addSubCategory(payload: { name: string; categoryId: number }): Observable<SubCategory> {
    return this.http.post<SubCategory>(`${this.apiBaseUrl}/sub-categories`, payload);
  }

  updateSubCategory(id: number, payload: { name: string; categoryId: number }): Observable<SubCategory> {
    return this.http.put<SubCategory>(`${this.apiBaseUrl}/sub-categories/${id}`, payload);
  }

  listTransactions(
    type: TransactionType,
    page = 0,
    size = 10,
    startDate?: string,
    endDate?: string
  ): Observable<ExpensePageResponse> {
    let params = new HttpParams();
    params = params.set('page', page);
    params = params.set('size', size);
    params = params.set('type', type);
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<ExpensePageResponse>(`${this.apiBaseUrl}/transactions`, { params });
  }

  addTransaction(payload: {
    amount: number;
    description: string;
    expenseDate: string;
    type: TransactionType;
    categoryId: number;
    subCategoryId: number;
  }): Observable<Expense> {
    return this.http.post<Expense>(`${this.apiBaseUrl}/transactions`, payload);
  }

  listExpenses(page = 0, size = 10, startDate?: string, endDate?: string): Observable<ExpensePageResponse> {
    let params = new HttpParams();
    params = params.set('page', page);
    params = params.set('size', size);
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<ExpensePageResponse>(`${this.apiBaseUrl}/expenses`, { params });
  }

  getDashboardSummary(topN = 5): Observable<DashboardSummaryResponse> {
    const params = new HttpParams().set('topN', topN);
    return this.http.get<DashboardSummaryResponse>(`${this.apiBaseUrl}/dashboard/summary`, { params });
  }

  addExpense(payload: {
    amount: number;
    description: string;
    expenseDate: string;
    categoryId: number;
    subCategoryId: number;
  }): Observable<Expense> {
    return this.http.post<Expense>(`${this.apiBaseUrl}/expenses`, payload);
  }
}
