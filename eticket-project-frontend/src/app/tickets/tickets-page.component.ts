import { ChangeDetectionStrategy, Component, computed } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { environment } from '../../environments/environment';
import { DiscountType, TicketType, TicketsPage } from './tickets.types';
import { Column, Variant, groupTickets, variantLabel } from './tickets.grouping';

@Component({
  selector: 'app-tickets-page',
  imports: [CurrencyPipe],
  templateUrl: './tickets-page.component.html',
  styleUrl: './tickets-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TicketsPageComponent {
  protected readonly ticketsRes = httpResource<TicketsPage>(() => ({
    url: `${environment.apiUrl}/tickets`,
    params: { size: 100 },
  }));

  protected readonly columns = computed<Column[]>(() =>
    groupTickets(this.ticketsRes.value()?._embedded?.ticketResponseList ?? []),
  );

  protected variantLabel(type: TicketType, v: Variant): string {
    return variantLabel(type, v);
  }

  protected buy(type: TicketType, variant: Variant, discount: DiscountType): void {
    // TODO
    console.log('buy', { type, durationMinutes: variant.durationMinutes, discount, price: variant.prices[discount] });
  }
}
