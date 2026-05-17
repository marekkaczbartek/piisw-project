package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TicketQueryRepository {
    List<Ticket> findAll();

    Page<Ticket> findAll(Pageable pageable);

    Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    );
}
