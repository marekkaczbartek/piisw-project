export type TicketType = 'SINGLE_USE' | 'TIME_BASED' | 'PERIOD';
export type DiscountType = 'NORMAL' | 'REDUCED';

export interface TicketResponse {
  ticketType: TicketType;
  price: number;
  discountType: DiscountType;
  durationMinutes: number | null;
}

export interface PageInfo {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface TicketsPage {
  _embedded?: {
    ticketResponseList?: TicketResponse[];
  };
  page: PageInfo;
}
