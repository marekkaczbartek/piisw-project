import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { roleGuard } from './auth/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'browse' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'browse',
    canActivate: [authGuard, roleGuard('PASSENGER')],
    loadComponent: () =>
      import('./browse/browse-page.component').then((m) => m.BrowsePageComponent),
  },
  {
    path: 'mine',
    canActivate: [authGuard, roleGuard('PASSENGER')],
    loadComponent: () =>
      import('./mine/my-tickets-page.component').then((m) => m.MyTicketsPageComponent),
  },
  {
    path: 'inspector',
    canActivate: [authGuard, roleGuard('INSPECTOR')],
    loadComponent: () =>
      import('./inspector/inspector-page.component').then((m) => m.InspectorPageComponent),
  },
  { path: '**', redirectTo: 'browse' },
];
