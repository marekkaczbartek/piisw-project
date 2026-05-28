import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserRole } from './auth.models';
import { AuthStore } from './auth.store';
import { homeRouteFor } from '../home-route';

export function roleGuard(required: UserRole): CanActivateFn {
  return () => {
    const store = inject(AuthStore);
    const router = inject(Router);

    const role = store.user()?.role;
    if (role === required) return true;

    return router.createUrlTree([homeRouteFor(role)]);
  };
}
