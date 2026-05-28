package org.example.eticket.application.service.ticket;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.mapper.ticket.TicketMapper;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.data.repositories.ticket.TicketQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketQueryRepository ticketReadRepository;
    private final TicketMapper ticketMapper;

    public List<TicketView> getAllTickets() {
        return ticketReadRepository.findAll()
                .stream()
                .map(ticketMapper::toView)
                .toList();
    }
}
