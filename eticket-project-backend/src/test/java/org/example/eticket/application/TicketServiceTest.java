package org.example.eticket.application;

import org.example.eticket.application.mapper.ticket.TicketMapper;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.application.service.ticket.TicketService;
import org.example.eticket.data.dto.TicketData;
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
        TicketData singleUse = new TicketData(
                null,
                TicketType.SINGLE_USE,
                DiscountType.NORMAL,
                new BigDecimal("3.50"),
                null
        );
        TicketData timeBased = new TicketData(
                null,
                TicketType.TIME_BASED,
                DiscountType.REDUCED,
                new BigDecimal("2.00"),
                30
        );
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
        TicketData first = new TicketData(
                null,
                TicketType.SINGLE_USE,
                DiscountType.NORMAL,
                new BigDecimal("3.50"),
                null
        );
        TicketData second = new TicketData(
                null,
                TicketType.TIME_BASED,
                DiscountType.REDUCED,
                new BigDecimal("2.00"),
                30
        );
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

    private record InMemoryTicketReadRepository(List<TicketData> tickets) implements TicketQueryRepository {

        private InMemoryTicketReadRepository(List<TicketData> tickets) {
            this.tickets = new ArrayList<>(tickets);
        }

        @Override
        public List<TicketData> findAll() {
            return List.copyOf(tickets);
        }

        @Override
        public Optional<TicketData> findByTicketTypeAndDiscountTypeAndDurationMinutes(
                TicketType ticketType,
                DiscountType discountType,
                Integer durationMinutes
        ) {
            return tickets.stream()
                    .filter(ticket -> ticketType == ticket.ticketType())
                    .filter(ticket -> discountType == ticket.discountType())
                    .filter(ticket -> durationMinutes == null
                            ? ticket.durationMinutes() == null
                            : durationMinutes.equals(ticket.durationMinutes()))
                    .findFirst();
        }
    }
}
