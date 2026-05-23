package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.entities.Validation;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.example.eticket.data.repositories.ValidationCommandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketValidationService {

    private final PurchaseQueryRepository purchaseQueryRepository;
    private final ValidationCommandRepository validationCommandRepository;
    private final UserResolver userResolver;

    public ValidationResultView isTicketValid(ValidateTicketCommand command, String inspectorEmail) {
        Purchase purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));
        User inspector = userResolver.resolveByEmail(inspectorEmail, "Inspector not found");
        boolean result = isValidForTicket(purchase, command);

        Validation validation = Validation.builder()
                .inspector(inspector)
                .purchase(purchase)
                .checkedAt(command.checkedAt())
                .checkedIn(command.checkedIn())
                .result(result)
                .build();
        validationCommandRepository.save(validation);

        return new ValidationResultView(result);
    }


    private static boolean isValidForTicket(Purchase purchase, ValidateTicketCommand command) {
        Ticket ticket = purchase.getTicket();
        if (ticket == null || ticket.getTicketType() == null) {
            return false;
        }

        return switch (ticket.getTicketType()) {
            case PERIOD -> isPeriodValid(purchase, command.checkedAt());
            case SINGLE_USE -> isSingleUseValid(purchase, command.checkedIn());
            case TIME_BASED -> isTimeBasedValid(purchase, command.checkedAt());
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

    private static boolean isSingleUseValid(Purchase purchase, String checkedIn) {
        if (purchase.getPunchedAt() == null) {
            return false;
        }
        String punchedIn = purchase.getPunchedIn();
        if (punchedIn == null || checkedIn == null) {
            return false;
        }
        return punchedIn.equals(checkedIn);
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
}
