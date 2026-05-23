package org.example.eticket.api.dto.purchase;

import jakarta.validation.constraints.NotBlank;

public record PunchTicketRequest(@NotBlank String punchedIn) {
}
