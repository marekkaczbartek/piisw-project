import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthStore } from './auth/auth.store';
import { landingUrlFor } from './auth/landing';
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

  protected readonly landingUrl = computed(() => landingUrlFor(this.store.user()?.role));

  logout(): void {
    this.store.clear();
    this.router.navigateByUrl('/login');
  }
}
