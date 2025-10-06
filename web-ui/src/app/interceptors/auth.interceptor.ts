import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  try {
    const token = (typeof window !== 'undefined')
      ? (localStorage.getItem('authToken') || sessionStorage.getItem('authToken'))
      : null;

    const apiPrefix = environment.apiBaseUrl.replace(/\/$/, '');
    const isApi = req.url.startsWith(apiPrefix);

    if (token && isApi) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  } catch {
    // no-op if storage is unavailable
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('authToken');
          sessionStorage.removeItem('authToken');
          localStorage.removeItem('currentUser');
          sessionStorage.removeItem('currentUser');
        }

        router.navigate(['/login'], {
          queryParams: { returnUrl: router.url, expired: 'true' }
        });
      }
      return throwError(() => error);
    })
  );
};
