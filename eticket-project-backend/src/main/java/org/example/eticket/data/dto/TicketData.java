package org.example.eticket.data.dto;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketData(
        UUID id,
        TicketType ticketType,
        DiscountType discountType,
        BigDecimal price,
        Integer durationMinutes
) {
}

