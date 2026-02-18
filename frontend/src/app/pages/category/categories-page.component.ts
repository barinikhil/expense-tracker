import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { BackendService, Category, CategoryType } from '../../services/backend.service';

@Component({
  selector: 'app-categories-page',
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
  templateUrl: './categories-page.component.html',
  styleUrl: './categories-page.component.css'
})
export class CategoriesPageComponent implements OnInit {
  error = '';
  categories: Category[] = [];

  readonly categoryTypes: { value: CategoryType; label: string }[] = [
    { value: 'EXPENSE', label: 'Expense' },
    { value: 'INCOME', label: 'Income' },
    { value: 'SAVING', label: 'Saving' }
  ];

  newCategory = { name: '', description: '', type: 'EXPENSE' as CategoryType };
  editingCategoryId: number | null = null;
  editingCategory = { name: '', description: '', type: 'EXPENSE' as CategoryType };

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.error = '';

    this.backendService.listCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.error = 'Failed to load categories.';
      }
    });
  }

  addCategory(): void {
    if (!this.newCategory.name.trim() || !this.newCategory.description.trim()) {
      return;
    }

    this.backendService.addCategory(this.newCategory).subscribe({
      next: () => {
        this.newCategory = { name: '', description: '', type: 'EXPENSE' as CategoryType };
        this.loadCategories();
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
      description: category.description,
      type: category.type ?? 'EXPENSE'
    };
  }

  saveCategoryEdit(): void {
    if (this.editingCategoryId === null) {
      return;
    }
    if (!this.editingCategory.name.trim() || !this.editingCategory.description.trim()) {
      this.error = 'Category name and description are required.';
      return;
    }

    this.backendService.updateCategory(this.editingCategoryId, this.editingCategory).subscribe({
      next: () => {
        this.cancelCategoryEdit();
        this.loadCategories();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to update category.';
      }
    });
  }

  cancelCategoryEdit(): void {
    this.editingCategoryId = null;
    this.editingCategory = { name: '', description: '', type: 'EXPENSE' as CategoryType };
  }

  subCategoryNames(category: Category): string {
    return category.subCategories.map((subCategory) => subCategory.name).join(', ');
  }
}
