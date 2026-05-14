package org.example.eticket.api.pagination;

import lombok.NonNull;
import org.example.eticket.api.controller.TicketController;
import org.example.eticket.api.dto.ticket.TicketResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TicketModelAssembler implements RepresentationModelAssembler<TicketResponse, EntityModel<TicketResponse>> {

    @Override
    public @NonNull EntityModel<TicketResponse> toModel(@NonNull TicketResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(TicketController.class).getAllTickets(null)).withRel("tickets"));
    }
}
