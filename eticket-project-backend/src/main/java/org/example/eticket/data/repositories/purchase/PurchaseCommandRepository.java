package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.entities.Purchase;

public interface PurchaseCommandRepository {
    Purchase save(Purchase purchase);
}

