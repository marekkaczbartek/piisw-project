export interface TicketValidationRequest {
  purchaseId: string;
  checkedAt: string; // LocalDateTime, no timezone suffix
  checkedIn: string;
}

export interface TicketValidationResponse {
  valid: boolean;
}

export interface ValidationEntry {
  id: string;
  checkedAt: string;
  vehicle: string;
  purchaseId: string;
  valid: boolean | null;
  error: boolean;
}
