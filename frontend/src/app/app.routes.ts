import { Routes } from '@angular/router';
import { CategoriesPageComponent } from './pages/categories-page.component';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'categories', pathMatch: 'full' },
  { path: 'categories', component: CategoriesPageComponent },
  { path: 'sub-categories', redirectTo: 'categories', pathMatch: 'full' },
  { path: '**', redirectTo: 'categories' }
];
