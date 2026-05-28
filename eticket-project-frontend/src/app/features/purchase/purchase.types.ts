import { DiscountType, TicketType } from '../browse/browse.types';

export interface MakePurchaseRequest {
  ticketType: TicketType;
  discountType: DiscountType;
  durationMinutes: number | null;
  boughtAt: string; // LocalDateTime, no timezone suffix (YYYY-MM-DDTHH:mm:ss)
}

export interface PurchaseResponse {
  id: string;
  ticketType: TicketType;
  discountType: DiscountType;
  price: number;
  durationMinutes: number | null;
  boughtAt: string;
  expiresAt: string | null;
}

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
