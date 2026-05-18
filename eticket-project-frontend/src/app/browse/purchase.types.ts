import { DiscountType, TicketType } from './browse.types';

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
