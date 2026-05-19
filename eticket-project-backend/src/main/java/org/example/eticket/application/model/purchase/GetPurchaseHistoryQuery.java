package org.example.eticket.application.model.purchase;

import org.springframework.data.domain.Pageable;

public record GetPurchaseHistoryQuery(Pageable pageable) {
}
