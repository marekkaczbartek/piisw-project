package org.example.eticket.data.repositories.ticket;

import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketQueryRepository {
    List<TicketData> findAll();

    Optional<TicketData> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    );
}
