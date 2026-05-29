package org.example.eticket.data.mapper;

import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.entities.Ticket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketDataMapper {
    TicketData toData(Ticket ticket);

    Ticket toEntity(TicketData data);
}

