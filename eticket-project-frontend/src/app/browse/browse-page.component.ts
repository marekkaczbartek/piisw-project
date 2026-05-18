import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { environment } from '../../environments/environment';
import { AuthStore } from '../auth/auth.store';
import { ToastService } from '../shared/toast.service';
import { DiscountType, TicketType, TicketsPage } from './browse.types';
import {
  Column,
  Variant,
  categoryLabel,
  groupTickets,
  variantDescription,
  variantLabel,
} from './browse.grouping';
import { CheckoutModalComponent, CheckoutSelection } from './checkout-modal.component';
import { PurchaseResponse } from './purchase.types';

@Component({
  selector: 'app-browse-page',
  imports: [CurrencyPipe, CheckoutModalComponent],
  templateUrl: './browse-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BrowsePageComponent {
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthStore);

  protected readonly ticketsRes = httpResource<TicketsPage>(() => ({
    url: `${environment.apiUrl}/tickets`,
    params: { size: 100 },
  }));

  protected readonly columns = computed<Column[]>(() =>
    groupTickets(this.ticketsRes.value()?._embedded?.ticketResponseList ?? []),
  );

  protected readonly selection = signal<CheckoutSelection | null>(null);

  protected openCheckout(type: TicketType, variant: Variant, discount: DiscountType): void {
    const price = variant.prices[discount];
    if (price == null) return;
    this.selection.set({ type, variant, discount, price });
  }

  protected closeCheckout(): void {
    this.selection.set(null);
  }

  protected onPurchased(p: PurchaseResponse, selection: CheckoutSelection): void {
    const label = variantLabel(selection.type, selection.variant);
    this.toast.show(`Zakupiono — ${categoryLabel(p.ticketType)} · ${label} · ${p.id}`);
    this.selection.set(null);
  }

  protected variantLabel(type: TicketType, v: Variant): string {
    return variantLabel(type, v);
  }

  protected variantDescription(type: TicketType, v: Variant): string {
    return variantDescription(type, v);
  }
}
