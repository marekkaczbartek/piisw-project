package org.example.eticket.repository;

import org.example.eticket.data.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {
}

