import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  subCategories: SubCategory[];
}

export interface SubCategory {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
}

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

@Injectable({
  providedIn: 'root'
})
export class BackendService {
  private readonly apiBaseUrl = 'http://192.168.6.35:9081/api';

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

  addCategory(payload: { name: string; description: string }): Observable<Category> {
    return this.http.post<Category>(`${this.apiBaseUrl}/categories`, payload);
  }

  updateCategory(id: number, payload: { name: string; description: string }): Observable<Category> {
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

  listExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(`${this.apiBaseUrl}/expenses`);
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
