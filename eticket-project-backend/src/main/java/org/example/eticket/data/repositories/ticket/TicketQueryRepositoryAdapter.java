package org.example.eticket.data.repositories.ticket;

import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.mapper.TicketDataMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketQueryRepositoryAdapter implements TicketQueryRepository {

    private final TicketJpaRepository ticketJpaRepository;
    private final TicketDataMapper ticketDataMapper;

    public TicketQueryRepositoryAdapter(TicketJpaRepository ticketJpaRepository, TicketDataMapper ticketDataMapper) {
        this.ticketJpaRepository = ticketJpaRepository;
        this.ticketDataMapper = ticketDataMapper;
    }

    @Override
    public List<TicketData> findAll() {
        return ticketJpaRepository.findAll().stream()
                .map(ticketDataMapper::toData)
                .toList();
    }

    @Override
    public Optional<TicketData> findByTicketTypeAndDiscountTypeAndDurationMinutes(
            TicketType ticketType,
            DiscountType discountType,
            Integer durationMinutes
    ) {
        return ticketJpaRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                ticketType,
                discountType,
                durationMinutes
        ).map(ticketDataMapper::toData);
    }
}
