package org.example.eticket.api;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.TicketResponse;
import org.example.eticket.service.TicketService;
import org.example.eticket.service.model.GetAllTicketsQuery;
import org.example.eticket.service.model.TicketView;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketModelAssembler ticketModelAssembler;
    private final PagedResourcesAssembler<TicketView> pagedResourceAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<TicketView> ticketPage = ticketService.getAllTickets(new GetAllTicketsQuery(page, size));
        PagedModel<EntityModel<TicketResponse>> model = pagedResourceAssembler.toModel(
                ticketPage,
                ticketView -> ticketModelAssembler.toModel(toResponse(ticketView))
        );
        return ResponseEntity.ok(model);
    }

    private static TicketResponse toResponse(TicketView ticketView) {
        return new TicketResponse(
                ticketView.ticketType(),
                ticketView.price(),
                ticketView.discountType(),
                ticketView.durationMinutes()
        );
    }
}
