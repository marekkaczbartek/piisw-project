import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'browse' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'browse',
    canActivate: [authGuard, roleGuard('PASSENGER')],
    loadComponent: () =>
      import('./features/browse/browse-page.component').then((m) => m.BrowsePageComponent),
  },
  {
    path: 'my-tickets',
    canActivate: [authGuard, roleGuard('PASSENGER')],
    loadComponent: () =>
      import('./features/my-tickets/my-tickets-page.component').then(
        (m) => m.MyTicketsPageComponent,
      ),
  },
  {
    path: 'inspector',
    canActivate: [authGuard, roleGuard('INSPECTOR')],
    loadComponent: () =>
      import('./features/inspector/inspector-page.component').then((m) => m.InspectorPageComponent),
  },
  { path: '**', redirectTo: 'browse' },
];
