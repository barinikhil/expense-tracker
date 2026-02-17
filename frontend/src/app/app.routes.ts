import { Routes } from '@angular/router';
import { CategoriesPageComponent } from './pages/category/categories-page.component';
import { SubCategoriesPageComponent } from './pages/sub-category/sub-categories-page.component';
import { LoginPageComponent } from './pages/auth/login-page.component';
import { authGuard } from './guards/auth.guard';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'categories', component: CategoriesPageComponent, canActivate: [authGuard] },
  { path: 'sub-categories', component: SubCategoriesPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];
