import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { httpResource } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { interval, map, startWith } from 'rxjs';
import { environment } from '../../environments/environment';
import { categoryLabel, variantLabel } from '../browse/browse.grouping';
import { TicketType } from '../browse/browse.types';
import { QrCodeComponent } from '../shared/qr-code.component';
import { PurchaseHistoryItem, PurchaseStatus, SpringPage } from './mine.types';
import { formatDuration, formatTimeUntil, purchaseStatus } from './purchase.status';

const PAGE_SIZE = 5;

interface EnrichedItem extends PurchaseHistoryItem {
  status: PurchaseStatus;
  label: string;
  categoryPl: string;
  subline: string;
}

@Component({
  selector: 'app-my-tickets-page',
  imports: [CurrencyPipe, DatePipe, RouterLink, QrCodeComponent],
  templateUrl: './my-tickets-page.component.html',
  styleUrl: './my-tickets-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyTicketsPageComponent {
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
    params: { size: PAGE_SIZE, page: this.page() },
  }));

  protected readonly tickets = computed<EnrichedItem[]>(() => {
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
      };
    });
  });

  protected readonly totalPages = computed(() => this.historyRes.value()?.totalPages ?? 0);
  protected readonly totalElements = computed(() => this.historyRes.value()?.totalElements ?? 0);

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
