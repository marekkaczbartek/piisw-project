package org.example.eticket.api.mapper.ticket;

import org.example.eticket.api.dto.ticket.TicketResponse;
import org.example.eticket.application.model.ticket.TicketView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketResponseMapper {

    TicketResponse toResponse(TicketView view);
}

