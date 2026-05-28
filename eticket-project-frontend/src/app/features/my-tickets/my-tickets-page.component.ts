import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { httpResource } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { interval, map, startWith } from 'rxjs';
import { environment } from '../../../environments/environment';
import { categoryLabel, variantLabel } from '../browse/browse.grouping';
import { TicketType } from '../browse/browse.types';
import { QrCodeComponent } from '../../shared/qr-code.component';
import { PurchaseHistoryItem, PurchaseStatus } from '../purchase/purchase.types';
import { formatDuration, formatTimeUntil, purchaseStatus } from '../purchase/purchase.status';
import { SpringPage } from '../../core/spring-page';

const PAGE_SIZE = 5;
// The usable/past split can't be expressed by the paginated backend, so fetch the
// whole history once and filter + paginate client-side.
const FETCH_SIZE = 500;

type TicketFilter = 'active' | 'history';

interface EnrichedItem extends PurchaseHistoryItem {
  status: PurchaseStatus;
  label: string;
  categoryPl: string;
  subline: string;
  past: boolean;
}

@Component({
  selector: 'app-my-tickets-page',
  imports: [CurrencyPipe, DatePipe, RouterLink, QrCodeComponent],
  templateUrl: './my-tickets-page.component.html',
  styleUrl: './my-tickets-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyTicketsPageComponent {
  protected readonly filter = signal<TicketFilter>('active');
  protected readonly page = signal(0);

  private readonly now = toSignal(
    interval(1000).pipe(
      startWith(0),
      map(() => new Date()),
    ),
    { initialValue: new Date() },
  );

  protected readonly historyRes = httpResource<SpringPage<PurchaseHistoryItem>>(() => ({
    url: `${environment.apiUrl}/purchases/history`,
    params: { size: FETCH_SIZE, page: 0 },
  }));

  private readonly allTickets = computed<EnrichedItem[]>(() => {
    const items = this.historyRes.value()?.content ?? [];
    const now = this.now();
    return items.map((p) => {
      const status = purchaseStatus(p, now);
      return {
        ...p,
        status,
        label: ticketLabel(p.ticketType, p.durationMinutes),
        categoryPl: categoryLabel(p.ticketType),
        subline: cardSubline(p, status, now),
        past: isPast(p, status),
      };
    });
  });

  protected readonly total = computed(() => this.allTickets().length);
  protected readonly activeCount = computed(() => this.allTickets().filter((t) => !t.past).length);
  protected readonly historyCount = computed(() => this.allTickets().filter((t) => t.past).length);

  private readonly filtered = computed(() => {
    const wantPast = this.filter() === 'history';
    return this.allTickets().filter((t) => t.past === wantPast);
  });

  protected readonly totalPages = computed(() => Math.ceil(this.filtered().length / PAGE_SIZE));
  protected readonly totalElements = computed(() => this.filtered().length);

  protected readonly tickets = computed<EnrichedItem[]>(() => {
    const start = this.page() * PAGE_SIZE;
    return this.filtered().slice(start, start + PAGE_SIZE);
  });

  protected setFilter(next: TicketFilter): void {
    if (this.filter() === next) return;
    this.filter.set(next);
    this.page.set(0);
  }

  protected prev(): void {
    if (this.page() > 0) this.page.update((p) => p - 1);
  }

  protected next(): void {
    if (this.page() < this.totalPages() - 1) this.page.update((p) => p + 1);
  }
}

function ticketLabel(type: TicketType, durationMinutes: number | null): string {
  return variantLabel(type, { durationMinutes, prices: {} });
}

function cardSubline(p: PurchaseHistoryItem, status: PurchaseStatus, now: Date): string {
  if (p.ticketType === 'SINGLE_USE') {
    if (!p.punchedAt) return 'Nieskasowany — skasuj w pojeździe';
    return `Skasowano · pojazd ${p.punchedIn ?? '—'}`;
  }
  if (p.expiresAt) {
    if (status.kind === 'expired') return 'Wygasł';
    const left = formatTimeUntil(p.expiresAt, now);
    return left ? `Wygasa za ${left}` : 'Wygasł';
  }
  return `Aktywuj — ważny ${formatDuration(p.durationMinutes)} od skasowania`;
}

// A single-use ticket is "done" once punched; time-based/period are done once expired.
function isPast(p: PurchaseHistoryItem, status: PurchaseStatus): boolean {
  if (p.ticketType === 'SINGLE_USE') return p.punchedAt != null;
  return status.kind === 'expired';
}
