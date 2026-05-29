package org.example.eticket.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseData(
        UUID id,
        UserData passenger,
        TicketData ticket,
        LocalDateTime boughtAt,
        LocalDateTime punchedAt,
        String punchedIn,
        LocalDateTime expiresAt
) {
}

