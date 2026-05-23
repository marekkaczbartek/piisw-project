package org.example.eticket.api.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TicketValidationRequest(@NotNull UUID purchaseId, @NotBlank String checkedIn) {
}
