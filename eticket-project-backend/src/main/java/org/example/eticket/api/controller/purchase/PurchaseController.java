package org.example.eticket.api.controller.purchase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.purchase.*;
import org.example.eticket.application.model.purchase.*;
import org.example.eticket.application.service.purchase.PurchaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PurchaseResponse> makePurchase(
            @Valid @RequestBody MakePurchaseRequest request,
            Authentication authentication
    ) {
        PurchaseView view = purchaseService.makePurchase(new MakePurchaseCommand(
                request.ticketType(),
                request.discountType(),
                request.durationMinutes(),
                request.boughtAt()
        ), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(view));
    }

    @GetMapping("/valid")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Page<ValidTicketResponse>> getValidTickets(
            @PageableDefault(size = 5) Pageable pageable,
            Authentication authentication
    ) {
        Page<ValidTicketResponse> response = purchaseService
                .getValidTickets(new GetValidPurchasesQuery(LocalDateTime.now(), pageable), authentication.getName())
                .map(PurchaseController::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Page<PurchaseHistoryResponse>> getPurchaseHistory(
            @PageableDefault(size = 5) Pageable pageable,
            Authentication authentication) {
        Page<PurchaseHistoryResponse> response = purchaseService
                .getPurchaseHistory(new GetPurchaseHistoryQuery(pageable), authentication.getName())
                .map(PurchaseController::toResponse);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{purchaseId}/punch")
    public ResponseEntity<PunchTicketResponse> punchTicket(
            @PathVariable("purchaseId") UUID purchaseId,
            @Valid @RequestBody PunchTicketRequest request) {

        PunchTicketView view = purchaseService.punchTicket(new PunchTicketCommand(
                purchaseId,
                LocalDateTime.now(),
                request.punchedIn()));
        return ResponseEntity.ok(toResponse(view));
    }

    private static PurchaseResponse toResponse(PurchaseView purchaseView) {
        return new PurchaseResponse(
                purchaseView.id(),
                purchaseView.ticketType(),
                purchaseView.discountType(),
                purchaseView.price(),
                purchaseView.durationMinutes(),
                purchaseView.boughtAt(),
                purchaseView.expiresAt()
        );
    }

    private static ValidTicketResponse toResponse(ValidPurchaseView view) {
        return new ValidTicketResponse(
                view.id(),
                view.ticketType(),
                view.discountType(),
                view.price(),
                view.durationMinutes(),
                view.boughtAt(),
                view.punchedAt(),
                view.punchedIn(),
                view.expiresAt()
        );
    }

    private static PurchaseHistoryResponse toResponse(PurchaseHistoryView view) {
        return new PurchaseHistoryResponse(
                view.id(),
                view.ticketType(),
                view.discountType(),
                view.price(),
                view.durationMinutes(),
                view.boughtAt(),
                view.punchedAt(),
                view.punchedIn(),
                view.expiresAt()
        );
    }

    private static PunchTicketResponse toResponse(PunchTicketView view) {
        return new PunchTicketResponse(
                view.id(),
                view.ticketType(),
                view.discountType(),
                view.price(),
                view.durationMinutes(),
                view.boughtAt(),
                view.punchedAt(),
                view.punchedIn(),
                view.expiresAt()
        );
    }
}
