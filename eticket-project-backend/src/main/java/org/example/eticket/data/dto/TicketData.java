package org.example.eticket.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketData {

    private UUID id;
    private TicketType ticketType;
    private DiscountType discountType;
    private BigDecimal price;
    private Integer durationMinutes;
}

