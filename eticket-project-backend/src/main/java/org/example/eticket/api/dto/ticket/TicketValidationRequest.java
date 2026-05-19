package org.example.eticket.api.dto.ticket;

import java.util.UUID;

public record TicketValidationRequest(UUID purchaseId, String checkedIn) {
}
