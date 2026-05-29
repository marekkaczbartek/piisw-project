package org.example.eticket.application.service.validation;

import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.dto.TicketData;

import java.time.LocalDateTime;

public class PurchaseValidator {

    public static boolean isValidAt(PurchaseData purchase, LocalDateTime checkedAt) {
        return isValid(purchase, checkedAt, null);
    }

    public static boolean isValidForInspection(PurchaseData purchase, LocalDateTime checkedAt, String checkedIn) {
        return isValid(purchase, checkedAt, checkedIn);
    }

    private static boolean isValid(PurchaseData purchase, LocalDateTime checkedAt, String checkedIn) {
        TicketData ticket = purchase.ticket();
        if (ticket == null || ticket.ticketType() == null) {
            return false;
        }

        return switch (ticket.ticketType()) {
            case PERIOD -> isPeriodValid(purchase, checkedAt);
            case SINGLE_USE -> isSingleUseValid(purchase, checkedIn);
            case TIME_BASED -> isTimeBasedValid(purchase, checkedAt);
        };
    }

    private static boolean isPeriodValid(PurchaseData purchase, LocalDateTime checkedAt) {
        LocalDateTime boughtAt = purchase.boughtAt();
        LocalDateTime expiresAt = purchase.expiresAt();
        if (boughtAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(boughtAt) && !checkedAt.isAfter(expiresAt);
    }

    private static boolean isSingleUseValid(PurchaseData purchase, String checkedIn) {
        if (purchase.expiresAt() != null) {
            return false;
        }
        if (purchase.punchedAt() == null || purchase.punchedIn() == null) {
            return false;
        }
        return purchase.punchedIn().equals(checkedIn);
    }

    private static boolean isTimeBasedValid(PurchaseData purchase, LocalDateTime checkedAt) {
        LocalDateTime punchedAt = purchase.punchedAt();
        LocalDateTime expiresAt = purchase.expiresAt();
        if (punchedAt == null || expiresAt == null) {
            return false;
        }
        return !checkedAt.isBefore(punchedAt) && !checkedAt.isAfter(expiresAt);
    }
}
