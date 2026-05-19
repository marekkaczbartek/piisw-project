package org.example.eticket.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.purchase.MakePurchaseRequest;
import org.example.eticket.api.dto.purchase.PurchaseResponse;
import org.example.eticket.api.dto.purchase.PurchaseHistoryResponse;
import org.example.eticket.api.dto.purchase.ValidTicketResponse;
import org.example.eticket.application.model.purchase.GetPurchaseHistoryQuery;
import org.example.eticket.application.model.purchase.GetValidTicketsQuery;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PurchaseHistoryView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.model.purchase.ValidTicketView;
import org.example.eticket.application.service.PurchaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

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

    private static ValidTicketResponse toResponse(ValidTicketView view) {
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

    @PostMapping
    public ResponseEntity<PurchaseResponse> makePurchase(@RequestBody MakePurchaseRequest request) {
        PurchaseView view = purchaseService.makePurchase(new MakePurchaseCommand(
                request.ticketType(),
                request.discountType(),
                request.durationMinutes(),
                request.boughtAt()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(view));
    }

    @GetMapping("/valid")
    public ResponseEntity<Page<ValidTicketResponse>> getValidTickets(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<ValidTicketResponse> response = purchaseService
                .getValidTickets(new GetValidTicketsQuery(LocalDateTime.now(), pageable))
                .map(PurchaseController::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PurchaseHistoryResponse>> getPurchaseHistory(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<PurchaseHistoryResponse> response = purchaseService
                .getPurchaseHistory(new GetPurchaseHistoryQuery(pageable))
                .map(PurchaseController::toResponse);
        return ResponseEntity.ok(response);
    }
}
