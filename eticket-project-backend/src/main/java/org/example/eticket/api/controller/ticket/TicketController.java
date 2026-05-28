package org.example.eticket.api.controller.ticket;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.ticket.TicketResponse;
import org.example.eticket.api.mapper.ticket.TicketResponseMapper;
import org.example.eticket.application.service.ticket.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketResponseMapper ticketResponseMapper;

    @GetMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<TicketResponse> response = ticketService.getAllTickets().stream()
                .map(ticketResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
