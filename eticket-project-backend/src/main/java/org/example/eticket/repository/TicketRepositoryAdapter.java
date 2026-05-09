package org.example.eticket.repository;

import org.example.eticket.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TicketRepositoryAdapter implements TicketReadRepository {

    private final TicketJpaRepository ticketJpaRepository;

    public TicketRepositoryAdapter(TicketJpaRepository ticketJpaRepository) {
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
