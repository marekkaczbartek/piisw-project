package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.entities.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseQueryRepository {

    Optional<Purchase> findById(UUID id);

    List<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId);

    Page<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId, Pageable pageable);
}
