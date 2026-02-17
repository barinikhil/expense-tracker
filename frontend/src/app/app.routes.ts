import { Routes } from '@angular/router';
import { CategoriesPageComponent } from './pages/categories-page.component';
import { SubCategoriesPageComponent } from './pages/sub-categories-page.component';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'categories', pathMatch: 'full' },
  { path: 'categories', component: CategoriesPageComponent },
  { path: 'sub-categories', component: SubCategoriesPageComponent },
  { path: '**', redirectTo: 'categories' }
];
