package org.example.eticket.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.purchase.MakePurchaseRequest;
import org.example.eticket.api.dto.purchase.PurchaseResponse;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

