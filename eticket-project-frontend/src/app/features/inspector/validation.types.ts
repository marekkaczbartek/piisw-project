export interface TicketValidationRequest {
  purchaseId: string;
  checkedIn: string;
}

export interface TicketValidationResponse {
  valid: boolean;
}
