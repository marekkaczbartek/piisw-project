package org.example.eticket.application.model.validation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ValidateTicketCommand(UUID purchaseId, LocalDateTime checkedAt, String checkedIn) {
}

