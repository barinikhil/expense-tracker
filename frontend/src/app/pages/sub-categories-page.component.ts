import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BackendService, Category, SubCategory } from '../services/backend.service';

@Component({
  selector: 'app-sub-categories-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sub-categories-page.component.html',
  styleUrl: './sub-categories-page.component.css'
})
export class SubCategoriesPageComponent implements OnInit {
  error = '';
  categories: Category[] = [];
  subCategories: SubCategory[] = [];

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
}
