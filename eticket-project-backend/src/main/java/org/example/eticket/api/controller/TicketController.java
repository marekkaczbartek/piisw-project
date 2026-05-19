package org.example.eticket.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.ticket.TicketResponse;
import org.example.eticket.application.model.ticket.TicketView;
import org.example.eticket.application.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    private static TicketResponse toResponse(TicketView ticketView) {
        return new TicketResponse(
                ticketView.ticketType(),
                ticketView.price(),
                ticketView.discountType(),
                ticketView.durationMinutes()
        );
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<TicketResponse> response = ticketService.getAllTickets().stream()
                .map(TicketController::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
