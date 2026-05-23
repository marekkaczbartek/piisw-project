package org.example.eticket.application.service.purchase;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.FieldRequiredException;
import org.example.eticket.application.exception.NotFoundException;
import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.exception.TicketAlreadyPunchedException;
import org.example.eticket.application.model.purchase.*;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.repositories.purchase.PurchaseCommandRepository;
import org.example.eticket.data.repositories.purchase.PurchaseQueryRepository;
import org.example.eticket.data.repositories.ticket.TicketQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketQueryRepository ticketQueryRepository;
    private final PurchaseCommandRepository purchaseCommandRepository;
    private final PurchaseQueryRepository purchaseQueryRepository;
    private final UserResolver userResolver;

    public PurchaseView makePurchase(MakePurchaseCommand command, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        Ticket ticket = ticketQueryRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                command.ticketType(),
                command.discountType(),
                command.durationMinutes()
        ).orElseThrow(() -> new NotFoundException("Ticket not found"));

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

    public PunchTicketView punchTicket(PunchTicketCommand command) {
        Purchase purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
        if (purchase.getPunchedAt() != null) {
            throw new TicketAlreadyPunchedException();
        }

        Ticket ticket = purchase.getTicket();
        if (ticket == null || ticket.getTicketType() == null) {
            throw new FieldRequiredException("ticketType");
        }

        LocalDateTime expiresAt = resolvePunchExpiry(ticket, command.punchedAt());
        purchase.setPunchedAt(command.punchedAt());
        purchase.setPunchedIn(command.punchedIn());
        purchase.setExpiresAt(expiresAt);

        Purchase saved = purchaseCommandRepository.save(purchase);
        return toPunchView(saved);
    }

    public Page<ValidPurchaseView> getValidTickets(GetValidPurchasesQuery query, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        List<ValidPurchaseView> filtered = purchaseQueryRepository
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

    public Page<PurchaseHistoryView> getPurchaseHistory(GetPurchaseHistoryQuery query, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        return purchaseQueryRepository.findAllByPassengerIdOrderByBoughtAtDesc(passenger.getId(), query.pageable())
                .map(PurchaseService::toHistoryView);
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

    private static LocalDateTime resolvePunchExpiry(Ticket ticket, LocalDateTime punchedAt) {
        if (ticket.getTicketType() == TicketType.PERIOD) {
            throw new PeriodTicketPunchNotAllowedException();
        }
        if (ticket.getTicketType() == TicketType.SINGLE_USE) {
            return null;
        }
        Integer durationMinutes = ticket.getDurationMinutes();
        if (durationMinutes == null) {
            throw new FieldRequiredException("durationMinutes");
        }
        return punchedAt.plusMinutes(durationMinutes);
    }

    private static ValidPurchaseView toValidTicketView(Purchase purchase) {
        Ticket ticket = purchase.getTicket();
        return new ValidPurchaseView(
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

    private static PunchTicketView toPunchView(Purchase purchase) {
        Ticket ticket = purchase.getTicket();
        return new PunchTicketView(
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
}
