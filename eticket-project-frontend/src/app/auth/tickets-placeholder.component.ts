import { Component, inject } from '@angular/core';
import { AuthStore } from './auth.store';

@Component({
  selector: 'app-tickets-placeholder',
  template: `
    <div class="alert alert-info">
      <strong>Signed in as {{ store.user()?.email }}.</strong>
      todo.
    </div>
  `,
})
export class TicketsPlaceholderComponent {
  protected readonly store = inject(AuthStore);
}
