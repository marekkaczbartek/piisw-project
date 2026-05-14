package org.example.eticket.api.dto.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketValidationRequest(UUID purchaseId, LocalDateTime checkedAt, String checkedIn) {
}

