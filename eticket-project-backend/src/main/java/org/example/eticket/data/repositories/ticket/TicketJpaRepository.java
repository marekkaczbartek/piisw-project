package org.example.eticket.data.repositories.ticket;

import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {
    boolean existsByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    );

    Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    );
}
