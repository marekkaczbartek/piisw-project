package org.example.eticket.application.model.ticket;

import org.springframework.data.domain.Pageable;

public record GetAllTicketsQuery(Pageable pageable) {
}
