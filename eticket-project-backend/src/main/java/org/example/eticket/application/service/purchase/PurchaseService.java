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
import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.dto.UserData;
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
        UserData passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        TicketData ticket = ticketQueryRepository.findByTicketTypeAndDiscountTypeAndDurationMinutes(
                command.ticketType(),
                command.discountType(),
                command.durationMinutes()
        ).orElseThrow(TicketNotFoundException::new);

        LocalDateTime expiresAt = resolveExpiry(ticket, command.boughtAt());

        PurchaseData purchase = new PurchaseData(
                null,
                passenger,
                ticket,
                command.boughtAt(),
                null,
                null,
                expiresAt
        );

        PurchaseData saved = purchaseCommandRepository.save(purchase);
        return purchaseMapper.toPurchaseView(saved);
    }

    public PunchTicketView punchTicket(PunchTicketCommand command) {
        PurchaseData purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(PurchaseNotFoundException::new);
        if (purchase.punchedAt() != null) {
            throw new TicketAlreadyPunchedException();
        }

        TicketData ticket = purchase.ticket();
        if (ticket == null || ticket.ticketType() == null) {
            throw new FieldRequiredException("ticketType");
        }

        LocalDateTime expiresAt = resolvePunchExpiry(ticket, command.punchedAt());
        PurchaseData updated = new PurchaseData(
                purchase.id(),
                purchase.passenger(),
                purchase.ticket(),
                purchase.boughtAt(),
                command.punchedAt(),
                command.punchedIn(),
                expiresAt
        );

        PurchaseData saved = purchaseCommandRepository.save(updated);
        return purchaseMapper.toPunchTicketView(saved);
    }

    public Page<ValidPurchaseView> getValidTickets(GetValidPurchasesQuery query, String passengerEmail) {
        UserData passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");

        List<PurchaseData> validPurchases = purchaseQueryRepository
                .findAllByPassengerIdOrderByBoughtAtDesc(passenger.id()).stream()
                .filter(purchase -> isValidAt(purchase, query.checkedAt()))
                .toList();

        return paginateAndMap(validPurchases, query.pageable(), purchaseMapper::toValidPurchaseView);
    }

    public Page<PurchaseHistoryView> getPurchaseHistory(GetPurchaseHistoryQuery query, String passengerEmail) {
        UserData passenger = userResolver.resolveByEmail(passengerEmail, "Passenger not found");
        return purchaseQueryRepository.findAllByPassengerIdOrderByBoughtAtDesc(passenger.id(), query.pageable())
                .map(purchaseMapper::toPurchaseHistoryView);
    }

    private static LocalDateTime resolveExpiry(TicketData ticket, LocalDateTime boughtAt) {
        if (ticket.ticketType() != TicketType.PERIOD) {
            return null;
        }
        Integer durationMinutes = ticket.durationMinutes();
        if (durationMinutes == null) {
            throw new FieldRequiredException("durationMinutes");
        }
        return boughtAt.plusMinutes(durationMinutes);
    }

    private static LocalDateTime resolvePunchExpiry(TicketData ticket, LocalDateTime punchedAt) {
        if (ticket.ticketType() == TicketType.PERIOD) {
            throw new PeriodTicketPunchNotAllowedException();
        }
        if (ticket.ticketType() == TicketType.SINGLE_USE) {
            return null;
        }
        Integer durationMinutes = ticket.durationMinutes();
        if (durationMinutes == null) {
            throw new FieldRequiredException("durationMinutes");
        }
        return punchedAt.plusMinutes(durationMinutes);
    }
}
