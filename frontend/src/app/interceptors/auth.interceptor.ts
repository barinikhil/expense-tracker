import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { SessionExpiredModalService } from '../services/session-expired-modal.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const sessionExpiredModalService = inject(SessionExpiredModalService);

  if (req.url.includes('/api/auth/login') || req.url.includes('/api/health')) {
    return next(req);
  }

  const token = authService.getToken();
  if (!token) {
    return next(req).pipe(
      catchError((error) => {
        handleAuthErrors(error.status, authService, router, sessionExpiredModalService);
        return throwError(() => error);
      })
    );
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  ).pipe(
    catchError((error) => {
      handleAuthErrors(error.status, authService, router, sessionExpiredModalService);
      return throwError(() => error);
    })
  );
};

function handleAuthErrors(
  status: number,
  authService: AuthService,
  router: Router,
  sessionExpiredModalService: SessionExpiredModalService
): void {
  if (status === 403) {
    authService.clearSession();
    sessionExpiredModalService.show('Access denied. Please log in again.');
    if (router.url !== '/login') {
      router.navigate(['/login']);
    }
    return;
  }

  if (status === 401) {
    authService.clearSession();
    if (router.url !== '/login') {
      router.navigate(['/login']);
    }
  }
}
