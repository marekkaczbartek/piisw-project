package org.example.eticket.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.ticket.TicketValidationRequest;
import org.example.eticket.api.dto.ticket.TicketValidationResponse;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.example.eticket.application.service.TicketValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/validations")
@RequiredArgsConstructor
public class ValidationController {

    private final TicketValidationService ticketValidationService;

    @PostMapping
    public ResponseEntity<TicketValidationResponse> isTicketValid(@RequestBody TicketValidationRequest request) {
        ValidationResultView view = ticketValidationService.isTicketValid(new ValidateTicketCommand(
                request.purchaseId(),
                LocalDateTime.now(),
                request.checkedIn()
        ));
        return ResponseEntity.ok(new TicketValidationResponse(view.valid()));
    }
}
