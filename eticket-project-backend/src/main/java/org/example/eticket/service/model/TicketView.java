package org.example.eticket.service.model;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.math.BigDecimal;

public record TicketView(
        TicketType ticketType,
        DiscountType discountType,
        BigDecimal price,
        Integer durationMinutes
) {
}

