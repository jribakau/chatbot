import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { CanActivateFn } from '@angular/router';
import { JwtService } from '../services/jwt.service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const jwtService = inject(JwtService);

  const token = jwtService.getToken();

  if (token && !jwtService.isTokenExpired()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
