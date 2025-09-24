import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const isBrowser = isPlatformBrowser(platformId);

  const token = isBrowser
    ? localStorage.getItem('authToken') || sessionStorage.getItem('authToken')
    : null;

  if (token) {
    return true;
  } 
  return router.createUrlTree(['/login']);
};