package org.example.eticket.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.eticket.enums.DiscountType;
import org.example.eticket.enums.TicketType;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketResponse(
        TicketType ticketType,
        BigDecimal price,
        DiscountType discountType,
        Integer durationMinutes
) {
}

