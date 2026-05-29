package org.example.eticket.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ValidationData(
        UUID id,
        UserData inspector,
        PurchaseData purchase,
        LocalDateTime checkedAt,
        String checkedIn,
        Boolean result
) {
}

