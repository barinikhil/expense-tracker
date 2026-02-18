import { Routes } from '@angular/router';
import { CategoriesPageComponent } from './pages/category/categories-page.component';
import { AddExpensePageComponent } from './pages/expense/add-expense-page.component';
import { ExpensesPageComponent } from './pages/expense/expenses-page.component';
import { DashboardPageComponent } from './pages/dashboard/dashboard-page.component';
import { SubCategoriesPageComponent } from './pages/sub-category/sub-categories-page.component';
import { LoginPageComponent } from './pages/auth/login-page.component';
import { authGuard } from './guards/auth.guard';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  { path: 'expenses', component: ExpensesPageComponent, canActivate: [authGuard] },
  { path: 'expenses/add', component: AddExpensePageComponent, canActivate: [authGuard] },
  { path: 'categories', component: CategoriesPageComponent, canActivate: [authGuard] },
  { path: 'sub-categories', component: SubCategoriesPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'dashboard' }
];
