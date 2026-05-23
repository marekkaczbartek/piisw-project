package org.example.eticket.application.service.ticket;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.repositories.ticket.TicketQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<TicketView> getAllTickets() {
        return ticketReadRepository.findAll()
                .stream()
                .map(TicketService::toView)
                .toList();
    }
}
