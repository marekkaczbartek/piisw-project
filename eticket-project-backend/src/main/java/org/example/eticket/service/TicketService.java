package org.example.eticket.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.repositories.TicketQueryRepository;
import org.example.eticket.service.model.GetAllTicketsQuery;
import org.example.eticket.service.model.TicketView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketQueryRepository ticketReadRepository;

    private static TicketView toView(Ticket ticket) {
        return new TicketView(
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getPrice(),
                ticket.getDurationMinutes()
        );
    }

    public Page<TicketView> getAllTickets(GetAllTicketsQuery query) {
        return ticketReadRepository.findAll(query.pageable())
                .map(TicketService::toView);
    }
}
