package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Purchase;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseQueryRepository {
    Optional<Purchase> findById(UUID id);
}

