package org.example.eticket.application.model.purchase;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseHistoryView(
        UUID id,
        TicketType ticketType,
        DiscountType discountType,
        BigDecimal price,
        Integer durationMinutes,
        LocalDateTime boughtAt,
        LocalDateTime punchedAt,
        String punchedIn,
        LocalDateTime expiresAt
) {
}

