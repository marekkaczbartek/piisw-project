package org.example.eticket.application.service.purchase;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.FieldRequiredException;
import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.exception.PurchaseNotFoundException;
import org.example.eticket.application.exception.TicketAlreadyPunchedException;
import org.example.eticket.application.exception.TicketNotFoundException;
import org.example.eticket.application.mapper.purchase.PurchaseMapper;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.eticket.api.pagination.PaginationUtils.paginateAndMap;
import static org.example.eticket.application.service.validation.PurchaseValidator.isValidAt;

// TODO refactor

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketQueryRepository ticketQueryRepository;
    private final PurchaseCommandRepository purchaseCommandRepository;
    private final PurchaseQueryRepository purchaseQueryRepository;
    private final UserResolver userResolver;
    private final PurchaseMapper purchaseMapper;

    public PurchaseView makePurchase(MakePurchaseCommand command, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        Ticket ticket = ticketQueryRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                command.ticketType(),
                command.discountType(),
                command.durationMinutes()
        ).orElseThrow(TicketNotFoundException::new);

        LocalDateTime expiresAt = resolveExpiry(ticket, command.boughtAt());

        Purchase purchase = Purchase.builder()
                .passenger(passenger)
                .ticket(ticket)
                .boughtAt(command.boughtAt())
                .expiresAt(expiresAt)
                .build();

        Purchase saved = purchaseCommandRepository.save(purchase);
        return purchaseMapper.toPurchaseView(saved);
    }

    public PunchTicketView punchTicket(PunchTicketCommand command) {
        Purchase purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(PurchaseNotFoundException::new);
        if (purchase.getPunchedAt() != null) {
            throw new TicketAlreadyPunchedException();
        }

        Ticket ticket = purchase.getTicket();
        if (ticket == null || ticket.getTicketType() == null) {
            throw new FieldRequiredException("ticketType");
        }

        LocalDateTime expiresAt = resolvePunchExpiry(ticket, command.punchedAt());
        // nie dzialac tu na encjach
        purchase.setPunchedAt(command.punchedAt());
        purchase.setPunchedIn(command.punchedIn());
        purchase.setExpiresAt(expiresAt);

        Purchase saved = purchaseCommandRepository.save(purchase);
        return purchaseMapper.toPunchTicketView(saved);
    }

    public Page<ValidPurchaseView> getValidTickets(GetValidPurchasesQuery query, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");

        List<Purchase> validPurchases = purchaseQueryRepository
                .findAllByPassengerIdOrderByBoughtAtDesc(passenger.getId()).stream()
                .filter(purchase -> isValidAt(purchase, query.checkedAt()))
                .toList();

        return paginateAndMap(validPurchases, query.pageable(), purchaseMapper::toValidPurchaseView);
    }

    public Page<PurchaseHistoryView> getPurchaseHistory(GetPurchaseHistoryQuery query, String passengerEmail) {
        User passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        return purchaseQueryRepository.findAllByPassengerIdOrderByBoughtAtDesc(passenger.getId(), query.pageable())
                .map(purchaseMapper::toPurchaseHistoryView);
    }

    private static LocalDateTime resolveExpiry(Ticket ticket, LocalDateTime boughtAt) {
        if (ticket.getTicketType() != TicketType.PERIOD) {
            return null;
        }
        Integer durationMinutes = ticket.getDurationMinutes();
        if (durationMinutes == null) {
            throw new FieldRequiredException("durationMinutes");
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
}
