import { Component, computed } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { environment } from '../../environments/environment';
import { DiscountType, TicketType, TicketsPage } from './tickets.types';

interface Variant {
  durationMinutes: number | null;
  prices: Partial<Record<DiscountType, number>>;
}

interface Column {
  type: TicketType;
  icon: string;
  title: string;
  variants: Variant[];
}

const TYPE_ORDER: TicketType[] = ['SINGLE_USE', 'TIME_BASED', 'PERIOD'];

const TYPE_META: Record<TicketType, { icon: string; title: string }> = {
  SINGLE_USE: { icon: '🎫', title: 'Jednorazowy' },
  TIME_BASED: { icon: '⏱️', title: 'Czasowy' },
  PERIOD:     { icon: '📅', title: 'Okresowy' },
};

@Component({
  selector: 'app-tickets-page',
  imports: [CurrencyPipe],
  templateUrl: './tickets-page.component.html',
  styleUrl: './tickets-page.component.scss',
})
export class TicketsPageComponent {
  protected readonly ticketsRes = httpResource<TicketsPage>(() => ({
    url: `${environment.apiUrl}/tickets`,
    params: { size: 100 },
  }));

  private readonly tickets = computed(
    () => this.ticketsRes.value()?._embedded?.ticketResponseList ?? [],
  );

  protected readonly columns = computed<Column[]>(() => {
    const byType = new Map<TicketType, Map<number | null, Variant>>();
    for (const type of TYPE_ORDER) byType.set(type, new Map());

    for (const t of this.tickets()) {
      const bucket = byType.get(t.ticketType);
      if (!bucket) continue;
      const key = t.durationMinutes;
      const existing = bucket.get(key) ?? { durationMinutes: key, prices: {} };
      existing.prices[t.discountType] = t.price;
      bucket.set(key, existing);
    }

    return TYPE_ORDER.map((type) => {
      const variants = [...(byType.get(type)?.values() ?? [])].sort((a, b) => {
        const av = a.durationMinutes ?? -1;
        const bv = b.durationMinutes ?? -1;
        return av - bv;
      });
      return { type, ...TYPE_META[type], variants };
    });
  });

  protected variantLabel(type: TicketType, v: Variant): string {
    if (type === 'SINGLE_USE') return 'Przejazd';
    if (v.durationMinutes == null) return '—';
    if (type === 'TIME_BASED') {
      return v.durationMinutes >= 60
        ? `${v.durationMinutes / 60} godz.`
        : `${v.durationMinutes} min`;
    }
    const days = Math.round(v.durationMinutes / (60 * 24));
    return days === 1 ? '1 dzień' : `${days} dni`;
  }

  protected buy(type: TicketType, variant: Variant, discount: DiscountType): void {
    console.log('buy', { type, durationMinutes: variant.durationMinutes, discount, price: variant.prices[discount] });
  }

  protected trackByVariant(_: number, v: Variant): string {
    return String(v.durationMinutes);
  }

  protected trackByColumn(_: number, c: Column): TicketType {
    return c.type;
  }
}
