package org.example.eticket.repository;

import org.example.eticket.data.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketReadRepository {
    List<Ticket> findAll();
    Page<Ticket> findAll(Pageable pageable);
}
