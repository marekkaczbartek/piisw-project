package org.example.eticket.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.ticket.TicketResponse;
import org.example.eticket.api.pagination.TicketModelAssembler;
import org.example.eticket.application.model.ticket.GetAllTicketsQuery;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.application.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketModelAssembler ticketModelAssembler;
    private final PagedResourcesAssembler<TicketView> pagedResourceAssembler;

    private static TicketResponse toResponse(TicketView ticketView) {
        return new TicketResponse(
                ticketView.ticketType(),
                ticketView.price(),
                ticketView.discountType(),
                ticketView.durationMinutes()
        );
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TicketResponse>>> getAllTickets(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<TicketView> ticketPage = ticketService.getAllTickets(new GetAllTicketsQuery(pageable));
        PagedModel<EntityModel<TicketResponse>> model = pagedResourceAssembler.toModel(
                ticketPage,
                ticketView -> ticketModelAssembler.toModel(toResponse(ticketView))
        );
        return ResponseEntity.ok(model);
    }
}
