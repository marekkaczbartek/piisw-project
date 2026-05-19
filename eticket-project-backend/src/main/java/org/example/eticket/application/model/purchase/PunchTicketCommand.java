package org.example.eticket.application.model.purchase;

import java.time.LocalDateTime;
import java.util.UUID;

public record PunchTicketCommand(UUID purchaseId, LocalDateTime punchedAt, String punchedIn) {
}

