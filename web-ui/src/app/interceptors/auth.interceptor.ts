import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
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
  return next(req);
};
