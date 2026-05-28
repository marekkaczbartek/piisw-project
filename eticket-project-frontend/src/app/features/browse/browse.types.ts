export type TicketType = 'SINGLE_USE' | 'TIME_BASED' | 'PERIOD';
export type DiscountType = 'NORMAL' | 'REDUCED';

export interface TicketResponse {
  ticketType: TicketType;
  price: number;
  discountType: DiscountType;
  durationMinutes: number | null;
}

export type TicketsResponse = TicketResponse[];
