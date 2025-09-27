import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  try {
    const token = (typeof window !== 'undefined')
      ? (localStorage.getItem('authToken') || sessionStorage.getItem('authToken'))
      : null;

    // Attach token only for our backend API
    const isApi = req.url.startsWith('http://localhost:8080/');
    if (token && isApi) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  } catch {
    // no-op if storage is unavailable
  }
  return next(req);
};
