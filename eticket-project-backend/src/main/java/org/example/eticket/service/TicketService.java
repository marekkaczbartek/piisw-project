package org.example.eticket.service;

import org.example.eticket.entity.Ticket;
import org.example.eticket.repository.TicketReadRepository;
import org.example.eticket.service.model.GetAllTicketsQuery;
import org.example.eticket.service.model.TicketView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final TicketReadRepository ticketReadRepository;

    public TicketService(TicketReadRepository ticketReadRepository) {
        this.ticketReadRepository = ticketReadRepository;
    }

    public Page<TicketView> getAllTickets(GetAllTicketsQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        return ticketReadRepository.findAll(pageRequest)
                .map(TicketService::toView);
    }

    private static TicketView toView(Ticket ticket) {
        return new TicketView(
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getPrice(),
                ticket.getDurationMinutes()
        );
    }
}
