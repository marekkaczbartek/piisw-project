package org.example.eticket.api.dto.purchase;

import java.time.LocalDateTime;

public record PunchTicketRequest(LocalDateTime punchedAt, String punchedIn) {
}

