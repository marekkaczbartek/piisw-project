package org.example.eticket.api.dto.purchase;

import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.time.LocalDateTime;

public record MakePurchaseRequest(
        TicketType ticketType,
        DiscountType discountType,
        Integer durationMinutes,
        LocalDateTime boughtAt
) {
}

