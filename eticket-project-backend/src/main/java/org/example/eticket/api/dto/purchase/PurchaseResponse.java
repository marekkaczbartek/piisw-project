package org.example.eticket.api.dto.purchase;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseResponse(
        UUID id,
        TicketType ticketType,
        DiscountType discountType,
        BigDecimal price,
        Integer durationMinutes,
        LocalDateTime boughtAt,
        LocalDateTime expiresAt
) {
}

