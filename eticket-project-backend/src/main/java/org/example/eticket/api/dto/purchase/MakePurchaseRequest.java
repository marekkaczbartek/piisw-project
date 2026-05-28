package org.example.eticket.api.dto.purchase;

import jakarta.validation.constraints.NotNull;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.time.LocalDateTime;

public record MakePurchaseRequest(
        @NotNull TicketType ticketType,
        @NotNull DiscountType discountType,
        Integer durationMinutes,
        @NotNull LocalDateTime boughtAt) {
}
