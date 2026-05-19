export interface TicketValidationRequest {
  purchaseId: string;
  checkedAt: string; // LocalDateTime, no timezone suffix
  checkedIn: string;
}

export interface TicketValidationResponse {
  valid: boolean;
}
