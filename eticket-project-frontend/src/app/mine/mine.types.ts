import { DiscountType, TicketType } from '../browse/browse.types';

export interface PurchaseHistoryItem {
  id: string;
  ticketType: TicketType;
  discountType: DiscountType;
  price: number;
  durationMinutes: number | null;
  boughtAt: string;
  punchedAt: string | null;
  punchedIn: string | null;
  expiresAt: string | null;
}

export type PurchaseStatusKind = 'active' | 'unused' | 'expired';

export interface PurchaseStatus {
  kind: PurchaseStatusKind;
  label: string;
}

/** Spring's `Page<T>` envelope, as serialized by Jackson. */
export interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
