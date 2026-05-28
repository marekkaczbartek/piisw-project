package org.example.eticket.application.mapper.ticket;

import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.data.entities.Ticket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketView toView(Ticket ticket);
}

