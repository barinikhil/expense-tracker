import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BackendService, Category, SubCategory } from './services/backend.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  status = 'Checking backend status...';
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
    this.loadStatus();
    this.loadData();
  }

  loadStatus(): void {
    this.backendService.getHealth().subscribe({
      next: (response) => {
        this.status = `Backend status: ${response.status}`;
      },
      error: () => {
        this.status = 'Backend status: DOWN';
      }
    });
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
