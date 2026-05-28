package org.example.eticket.application;

import org.example.eticket.application.mapper.ticket.TicketMapper;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.application.service.ticket.TicketService;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.repositories.ticket.TicketQueryRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketServiceTest {

    @Test
    void returnsEmptyListWhenNoTicketsExist() {
        // given
        TicketQueryRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of());
        TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);
        TicketService ticketService = new TicketService(ticketReadRepository, ticketMapper);

        // when
        List<TicketView> result = ticketService.getAllTickets();

        // then
        assertEquals(List.of(), result);
    }

    @Test
    void returnsAllTicketsWithDurationIfProvided() {
        // given
        Ticket singleUse = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .durationMinutes(null)
                .build();
        Ticket timeBased = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("2.00"))
                .durationMinutes(30)
                .build();
        TicketQueryRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of(singleUse, timeBased));
        TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);
        TicketService ticketService = new TicketService(ticketReadRepository, ticketMapper);

        // when
        List<TicketView> result = ticketService.getAllTickets();

        // then
        List<TicketView> expected = List.of(
                new TicketView(TicketType.SINGLE_USE, DiscountType.NORMAL, new BigDecimal("3.50"), null),
                new TicketView(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("2.00"), 30)
        );
        assertEquals(expected, result);
    }

    @Test
    void returnsAllTicketsWhenRequested() {
        // given
        Ticket first = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .build();
        Ticket second = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("2.00"))
                .durationMinutes(30)
                .build();
        TicketQueryRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of(first, second));
        TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);
        TicketService ticketService = new TicketService(ticketReadRepository, ticketMapper);

        // when
        List<TicketView> result = ticketService.getAllTickets();

        // then
        List<TicketView> expected = List.of(
                new TicketView(TicketType.SINGLE_USE, DiscountType.NORMAL, new BigDecimal("3.50"), null),
                new TicketView(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("2.00"), 30)
        );
        assertEquals(expected, result);
    }

    private record InMemoryTicketReadRepository(List<Ticket> tickets) implements TicketQueryRepository {

        private InMemoryTicketReadRepository(List<Ticket> tickets) {
            this.tickets = new ArrayList<>(tickets);
        }

        @Override
        public List<Ticket> findAll() {
            return List.copyOf(tickets);
        }

        @Override
        public Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
                TicketType ticketType,
                DiscountType discountType,
                Integer durationMinutes
        ) {
            return tickets.stream()
                    .filter(ticket -> ticketType == ticket.getTicketType())
                    .filter(ticket -> discountType == ticket.getDiscountType())
                    .filter(ticket -> durationMinutes == null
                            ? ticket.getDurationMinutes() == null
                            : durationMinutes.equals(ticket.getDurationMinutes()))
                    .findFirst();
        }
    }
}
