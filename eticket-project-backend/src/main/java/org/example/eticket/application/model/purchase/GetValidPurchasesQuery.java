package org.example.eticket.application.model.purchase;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public record GetValidPurchasesQuery(LocalDateTime checkedAt, Pageable pageable) {
}
