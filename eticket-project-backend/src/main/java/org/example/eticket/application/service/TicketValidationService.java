package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.entities.Validation;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.example.eticket.data.repositories.ValidationCommandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketValidationService {

    private final PurchaseQueryRepository purchaseQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final ValidationCommandRepository validationCommandRepository;

    public ValidationResultView isTicketValid(ValidateTicketCommand command) {
        if (command.checkedAt() == null || command.checkedIn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkedAt and checkedIn are required");
        }

        Purchase purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));
        User inspector = resolveInspector();
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

    private User resolveInspector() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inspector not authenticated");
        }

        String email = authentication.getName();
        User inspector = userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inspector not found"));
        if (inspector.getRole() != UserRole.INSPECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Inspector role required");
        }
        return inspector;
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
