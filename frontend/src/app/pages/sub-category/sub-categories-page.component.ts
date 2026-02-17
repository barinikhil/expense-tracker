import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService, Category, SubCategory } from '../../services/backend.service';

@Component({
  selector: 'app-sub-categories-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './sub-categories-page.component.html',
  styleUrl: './sub-categories-page.component.css'
})
export class SubCategoriesPageComponent implements OnInit {
  error = '';
  categories: Category[] = [];
  subCategories: SubCategory[] = [];

  newSubCategory: { name: string; categoryId: number | null } = { name: '', categoryId: null };
  subCategoryFilterCategoryId = 0;
  editingSubCategoryId: number | null = null;
  editingSubCategory: { name: string; categoryId: number | null } = { name: '', categoryId: null };

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

  addSubCategory(): void {
    if (!this.newSubCategory.name.trim() || this.newSubCategory.categoryId === null) {
      return;
    }
    const payload = {
      name: this.newSubCategory.name.trim(),
      categoryId: this.newSubCategory.categoryId
    };

    this.backendService.addSubCategory(payload).subscribe({
      next: () => {
        this.newSubCategory = { name: '', categoryId: null };
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
    if (!this.editingSubCategory.name.trim() || this.editingSubCategory.categoryId === null) {
      this.error = 'Sub-category name and category are required.';
      return;
    }
    const payload = {
      name: this.editingSubCategory.name.trim(),
      categoryId: this.editingSubCategory.categoryId
    };

    this.backendService.updateSubCategory(this.editingSubCategoryId, payload).subscribe({
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
    this.editingSubCategory = { name: '', categoryId: null };
  }

  get filteredSubCategories(): SubCategory[] {
    if (!this.subCategoryFilterCategoryId) {
      return this.subCategories;
    }

    return this.subCategories.filter(
      (subCategory) => subCategory.categoryId === this.subCategoryFilterCategoryId
    );
  }
}
