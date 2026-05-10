package org.example.eticket.service.model;

import org.springframework.data.domain.Pageable;

public record GetAllTicketsQuery(Pageable pageable) {
}
