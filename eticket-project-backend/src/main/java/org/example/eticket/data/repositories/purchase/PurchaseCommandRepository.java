package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.dto.PurchaseData;

public interface PurchaseCommandRepository {
    PurchaseData save(PurchaseData purchase);
}

