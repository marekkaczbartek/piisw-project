package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.model.purchase.GetPurchaseHistoryQuery;
import org.example.eticket.application.model.purchase.GetValidTicketsQuery;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PurchaseHistoryView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.model.purchase.ValidTicketView;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.PurchaseCommandRepository;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.example.eticket.data.repositories.TicketQueryRepository;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketQueryRepository ticketQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final PurchaseCommandRepository purchaseCommandRepository;
    private final PurchaseQueryRepository purchaseQueryRepository;

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

    public Page<ValidTicketView> getValidTickets(GetValidTicketsQuery query) {
        if (query.checkedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkedAt is required");
        }
        if (query.pageable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pageable is required");
        }
        User passenger = resolvePassenger();
        List<ValidTicketView> filtered = purchaseQueryRepository
                .findAllByPassengerIdOrderByBoughtAtDesc(passenger.getId()).stream()
                .filter(purchase -> isValidAt(purchase, query.checkedAt()))
                .map(PurchaseService::toValidTicketView)
                .toList();
        Pageable pageable = query.pageable();
        int start = Math.toIntExact(pageable.getOffset());
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    public Page<PurchaseHistoryView> getPurchaseHistory(GetPurchaseHistoryQuery query) {
        if (query.pageable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pageable is required");
        }
        User passenger = resolvePassenger();
        return purchaseQueryRepository.findAllByPassengerIdOrderByBoughtAtDesc(passenger.getId(), query.pageable())
                .map(PurchaseService::toHistoryView);
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

    private static ValidTicketView toValidTicketView(Purchase purchase) {
        Ticket ticket = purchase.getTicket();
        return new ValidTicketView(
                purchase.getId(),
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getPrice(),
                ticket.getDurationMinutes(),
                purchase.getBoughtAt(),
                purchase.getPunchedAt(),
                purchase.getPunchedIn(),
                resolveExpiry(purchase)
        );
    }

    private static PurchaseHistoryView toHistoryView(Purchase purchase) {
        Ticket ticket = purchase.getTicket();
        return new PurchaseHistoryView(
                purchase.getId(),
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getPrice(),
                ticket.getDurationMinutes(),
                purchase.getBoughtAt(),
                purchase.getPunchedAt(),
                purchase.getPunchedIn(),
                purchase.getExpiresAt()
        );
    }

    private static boolean isValidAt(Purchase purchase, LocalDateTime checkedAt) {
        Ticket ticket = purchase.getTicket();
        if (ticket == null || ticket.getTicketType() == null) {
            return false;
        }

        return switch (ticket.getTicketType()) {
            case PERIOD -> isPeriodValid(purchase, checkedAt);
            case SINGLE_USE -> isSingleUseValid(purchase);
            case TIME_BASED -> isTimeBasedValid(purchase, checkedAt);
        };
    }

    private static boolean isPeriodValid(Purchase purchase, LocalDateTime checkedAt) {
        LocalDateTime boughtAt = purchase.getBoughtAt();
        LocalDateTime expiresAt = resolvePeriodExpiry(purchase);
        if (checkedAt == null || boughtAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(boughtAt) && !checkedAt.isAfter(expiresAt);
    }

    private static LocalDateTime resolvePeriodExpiry(Purchase purchase) {
        if (purchase.getExpiresAt() != null) {
            return purchase.getExpiresAt();
        }
        if (purchase.getTicket() == null || purchase.getTicket().getDurationMinutes() == null) {
            return null;
        }
        if (purchase.getBoughtAt() == null) {
            return null;
        }
        return purchase.getBoughtAt().plusMinutes(purchase.getTicket().getDurationMinutes());
    }

    private static boolean isSingleUseValid(Purchase purchase) {
        return purchase.getPunchedAt() != null && purchase.getPunchedIn() != null;
    }

    private static boolean isTimeBasedValid(Purchase purchase, LocalDateTime checkedAt) {
        LocalDateTime punchedAt = purchase.getPunchedAt();
        LocalDateTime expiresAt = resolveTimeBasedExpiry(purchase);
        if (checkedAt == null || punchedAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(punchedAt) && !checkedAt.isAfter(expiresAt);
    }

    private static LocalDateTime resolveTimeBasedExpiry(Purchase purchase) {
        if (purchase.getExpiresAt() != null) {
            return purchase.getExpiresAt();
        }
        if (purchase.getTicket() == null || purchase.getTicket().getDurationMinutes() == null) {
            return null;
        }
        if (purchase.getPunchedAt() == null) {
            return null;
        }
        return purchase.getPunchedAt().plusMinutes(purchase.getTicket().getDurationMinutes());
    }

    private static LocalDateTime resolveExpiry(Purchase purchase) {
        TicketType ticketType = purchase.getTicket() != null ? purchase.getTicket().getTicketType() : null;
        if (ticketType == null) {
            return purchase.getExpiresAt();
        }
        return switch (ticketType) {
            case PERIOD -> resolvePeriodExpiry(purchase);
            case TIME_BASED -> resolveTimeBasedExpiry(purchase);
            case SINGLE_USE -> purchase.getExpiresAt();
        };
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
