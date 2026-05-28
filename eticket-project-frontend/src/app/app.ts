import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthStore } from './core/auth/auth.store';
import { homeRouteFor } from './core/home-route';
import { ToastComponent } from './shared/toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly store = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly homeRoute = computed(() => homeRouteFor(this.store.user()?.role));

  logout(): void {
    this.store.clear();
    this.router.navigateByUrl('/login');
  }
}
