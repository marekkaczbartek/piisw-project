package org.example.eticket.application.service.validation;

import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;

import java.time.LocalDateTime;

public class PurchaseValidator {

    public static boolean isValidAt(Purchase purchase, LocalDateTime checkedAt) {
        return isValid(purchase, checkedAt, null);
    }

    public static boolean isValidForInspection(Purchase purchase, LocalDateTime checkedAt, String checkedIn) {
        return isValid(purchase, checkedAt, checkedIn);
    }

    private static boolean isValid(Purchase purchase, LocalDateTime checkedAt, String checkedIn) {
        Ticket ticket = purchase.getTicket();
        if (ticket == null || ticket.getTicketType() == null) {
            return false;
        }

        return switch (ticket.getTicketType()) {
            case PERIOD -> isPeriodValid(purchase, checkedAt);
            case SINGLE_USE -> isSingleUseValid(purchase, checkedIn);
            case TIME_BASED -> isTimeBasedValid(purchase, checkedAt);
        };
    }

    private static boolean isPeriodValid(Purchase purchase, LocalDateTime checkedAt) {
        LocalDateTime boughtAt = purchase.getBoughtAt();
        LocalDateTime expiresAt = purchase.getExpiresAt();
        if (boughtAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(boughtAt) && !checkedAt.isAfter(expiresAt);
    }

    private static boolean isSingleUseValid(Purchase purchase, String checkedIn) {
        if (purchase.getPunchedAt() == null || purchase.getPunchedIn() == null) {
            return false;
        }
        return purchase.getPunchedIn().equals(checkedIn);
    }

    private static boolean isTimeBasedValid(Purchase purchase, LocalDateTime checkedAt) {
        LocalDateTime punchedAt = purchase.getPunchedAt();
        LocalDateTime expiresAt = purchase.getExpiresAt();
        if (punchedAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(punchedAt) && !checkedAt.isAfter(expiresAt);
    }
}

