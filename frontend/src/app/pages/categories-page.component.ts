import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { BackendService, Category, SubCategory } from '../services/backend.service';

@Component({
  selector: 'app-categories-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './categories-page.component.html',
  styleUrl: './categories-page.component.css'
})
export class CategoriesPageComponent implements OnInit {
  error = '';
  categories: Category[] = [];
  subCategories: SubCategory[] = [];

  newCategory = { name: '', description: '' };
  editingCategoryId: number | null = null;
  editingCategory = { name: '', description: '' };

  newSubCategory = { name: '', categoryId: 0 };
  editingSubCategoryId: number | null = null;
  editingSubCategory = { name: '', categoryId: 0 };

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.error = '';

    this.backendService.listCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.error = 'Failed to load categories.';
      }
    });

    this.backendService.listSubCategories().subscribe({
      next: (subCategories) => {
        this.subCategories = subCategories;
      },
      error: () => {
        this.error = 'Failed to load sub-categories.';
      }
    });
  }

  addCategory(): void {
    if (!this.newCategory.name.trim() || !this.newCategory.description.trim()) {
      return;
    }

    this.backendService.addCategory(this.newCategory).subscribe({
      next: () => {
        this.newCategory = { name: '', description: '' };
        this.loadData();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to add category.';
      }
    });
  }

  startEditCategory(category: Category): void {
    this.editingCategoryId = category.id;
    this.editingCategory = {
      name: category.name,
      description: category.description
    };
  }

  saveCategoryEdit(): void {
    if (this.editingCategoryId === null) {
      return;
    }

    this.backendService.updateCategory(this.editingCategoryId, this.editingCategory).subscribe({
      next: () => {
        this.cancelCategoryEdit();
        this.loadData();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to update category.';
      }
    });
  }

  cancelCategoryEdit(): void {
    this.editingCategoryId = null;
    this.editingCategory = { name: '', description: '' };
  }

  addSubCategory(): void {
    if (!this.newSubCategory.name.trim() || !this.newSubCategory.categoryId) {
      return;
    }

    this.backendService.addSubCategory(this.newSubCategory).subscribe({
      next: () => {
        this.newSubCategory = { name: '', categoryId: 0 };
        this.loadData();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to add sub-category.';
      }
    });
  }

  startEditSubCategory(subCategory: SubCategory): void {
    this.editingSubCategoryId = subCategory.id;
    this.editingSubCategory = {
      name: subCategory.name,
      categoryId: subCategory.categoryId
    };
  }

  saveSubCategoryEdit(): void {
    if (this.editingSubCategoryId === null) {
      return;
    }

    this.backendService.updateSubCategory(this.editingSubCategoryId, this.editingSubCategory).subscribe({
      next: () => {
        this.cancelSubCategoryEdit();
        this.loadData();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to update sub-category.';
      }
    });
  }

  cancelSubCategoryEdit(): void {
    this.editingSubCategoryId = null;
    this.editingSubCategory = { name: '', categoryId: 0 };
  }

  subCategoryNames(category: Category): string {
    return category.subCategories.map((subCategory) => subCategory.name).join(', ');
  }
}
