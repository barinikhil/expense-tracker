import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BackendService } from '../../services/backend.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  credentials = { username: '', password: '' };
  error = '';
  submitting = false;

  constructor(
    private readonly backendService: BackendService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  login(): void {
    if (!this.credentials.username.trim() || !this.credentials.password.trim() || this.submitting) {
      return;
    }

    this.error = '';
    this.submitting = true;

    this.backendService.login(this.credentials).subscribe({
      next: (response) => {
        this.authService.setSession(response.token, response.username);
        this.credentials = { username: '', password: '' };
        this.submitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Invalid username or password.';
        this.submitting = false;
      }
    });
  }
}
