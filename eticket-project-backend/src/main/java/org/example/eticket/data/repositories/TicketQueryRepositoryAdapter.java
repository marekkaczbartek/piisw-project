package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketJpaRepository.findAll(pageable);
    }
}
