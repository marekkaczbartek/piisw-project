package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketQueryRepositoryAdapter implements TicketQueryRepository {

    private final TicketJpaRepository ticketJpaRepository;

    public TicketQueryRepositoryAdapter(TicketJpaRepository ticketJpaRepository) {
        this.ticketJpaRepository = ticketJpaRepository;
    }

    @Override
    public List<Ticket> findAll() {
        return ticketJpaRepository.findAll();
    }

    @Override
    public Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    ) {
        return ticketJpaRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                ticketType,
                discountType,
                durationMinutes
        );
    }
}
