package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketQueryRepository {
    List<Ticket> findAll();

    Page<Ticket> findAll(Pageable pageable);
}
