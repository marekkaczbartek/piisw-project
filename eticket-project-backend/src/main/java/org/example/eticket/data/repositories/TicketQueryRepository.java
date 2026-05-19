package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketQueryRepository {
    List<Ticket> findAll();

    Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    );
}
