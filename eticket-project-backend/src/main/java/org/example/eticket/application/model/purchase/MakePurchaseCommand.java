package org.example.eticket.application.model.purchase;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.time.LocalDateTime;

public record MakePurchaseCommand(
        TicketType ticketType,
        DiscountType discountType,
        Integer durationMinutes,
        LocalDateTime boughtAt
) {
}

