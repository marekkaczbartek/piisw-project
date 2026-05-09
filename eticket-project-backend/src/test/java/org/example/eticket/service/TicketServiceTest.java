package org.example.eticket.service;

import org.example.eticket.entity.Ticket;
import org.example.eticket.enums.DiscountType;
import org.example.eticket.enums.TicketType;
import org.example.eticket.repository.TicketReadRepository;
import org.example.eticket.service.model.GetAllTicketsQuery;
import org.example.eticket.service.model.TicketView;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketServiceTest {

    @Test
    void returnsEmptyListWhenNoTicketsExist() {
        // given
        TicketReadRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of());
        TicketService ticketService = new TicketService(ticketReadRepository);

        // when
        List<TicketView> result = ticketService.getAllTickets(new GetAllTicketsQuery(0, 20)).getContent();

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
        TicketReadRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of(singleUse, timeBased));
        TicketService ticketService = new TicketService(ticketReadRepository);

        // when
        List<TicketView> result = ticketService.getAllTickets(new GetAllTicketsQuery(0, 20)).getContent();

        // then
        List<TicketView> expected = List.of(
                new TicketView(TicketType.SINGLE_USE, DiscountType.NORMAL, new BigDecimal("3.50"), null),
                new TicketView(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("2.00"), 30)
        );
        assertEquals(expected, result);
    }

    @Test
    void returnsRequestedPageOfTickets() {
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
        TicketReadRepository ticketReadRepository = new InMemoryTicketReadRepository(List.of(first, second));
        TicketService ticketService = new TicketService(ticketReadRepository);

        // when
        List<TicketView> result = ticketService.getAllTickets(new GetAllTicketsQuery(1, 1)).getContent();

        // then
        List<TicketView> expected = List.of(
                new TicketView(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("2.00"), 30)
        );
        assertEquals(expected, result);
    }

    private static class InMemoryTicketReadRepository implements TicketReadRepository {

        private final List<Ticket> tickets;

        private InMemoryTicketReadRepository(List<Ticket> tickets) {
            this.tickets = new ArrayList<>(tickets);
        }

        @Override
        public List<Ticket> findAll() {
            return List.copyOf(tickets);
        }

        @Override
        public Page<Ticket> findAll(Pageable pageable) {
            int start = Math.toIntExact(pageable.getOffset());
            if (start >= tickets.size()) {
                return new PageImpl<>(List.of(), pageable, tickets.size());
            }
            int end = Math.min(start + pageable.getPageSize(), tickets.size());
            return new PageImpl<>(tickets.subList(start, end), pageable, tickets.size());
        }
    }
}
