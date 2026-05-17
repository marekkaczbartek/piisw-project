package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.PurchaseCommandRepository;
import org.example.eticket.data.repositories.TicketQueryRepository;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketQueryRepository ticketQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final PurchaseCommandRepository purchaseCommandRepository;

    public PurchaseView makePurchase(MakePurchaseCommand command) {
        if (command.boughtAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boughtAt is required");
        }
        User passenger = resolvePassenger();
        Ticket ticket = ticketQueryRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                command.ticketType(),
                command.discountType(),
                command.durationMinutes()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        LocalDateTime expiresAt = resolveExpiry(ticket, command.boughtAt());

        Purchase purchase = Purchase.builder()
                .passenger(passenger)
                .ticket(ticket)
                .boughtAt(command.boughtAt())
                .expiresAt(expiresAt)
                .build();

        Purchase saved = purchaseCommandRepository.save(purchase);
        return toView(saved);
    }

    private User resolvePassenger() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passenger not authenticated");
        }

        String email = authentication.getName();
        User passenger = userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passenger not found"));
        if (passenger.getRole() != UserRole.PASSENGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Passenger role required");
        }
        return passenger;
    }

    private static LocalDateTime resolveExpiry(Ticket ticket, LocalDateTime boughtAt) {
        if (ticket.getTicketType() != TicketType.PERIOD) {
            return null;
        }
        Integer durationMinutes = ticket.getDurationMinutes();
        if (durationMinutes == null) {
            return null;
        }
        return boughtAt.plusMinutes(durationMinutes);
    }

    private static PurchaseView toView(Purchase purchase) {
        Ticket ticket = purchase.getTicket();
        return new PurchaseView(
                purchase.getId(),
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getPrice(),
                ticket.getDurationMinutes(),
                purchase.getBoughtAt(),
                purchase.getExpiresAt()
        );
    }
}
