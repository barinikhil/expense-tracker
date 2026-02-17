import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { of, Subject, timer } from 'rxjs';
import { catchError, switchMap, takeUntil, tap, timeout } from 'rxjs/operators';
import { BackendService } from './services/backend.service';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatCardModule,
    MatListModule,
    MatIconModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  status = 'Checking backend status...';
  isBackendUp = false;
  retryCountdownSeconds = 0;
  private readonly retryIntervalSeconds = Math.max(1, Math.ceil(environment.healthCheckIntervalMs / 1000));
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    timer(0, environment.healthCheckIntervalMs)
      .pipe(
        tap(() => {
          this.retryCountdownSeconds = this.retryIntervalSeconds;
        }),
        switchMap(() =>
          this.backendService.getHealth().pipe(
            timeout(2000),
            catchError(() => of({ status: 'DOWN' }))
          )
        ),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response) => {
          this.status = `Backend status: ${response.status}`;
          this.isBackendUp = response.status?.toUpperCase() === 'UP';
          if (this.isBackendUp) {
            this.retryCountdownSeconds = 0;
          }
        }
      });

    timer(1000, 1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (!this.isBackendUp && this.retryCountdownSeconds > 0) {
          this.retryCountdownSeconds -= 1;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
