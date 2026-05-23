package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.entities.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, UUID> {
    List<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId);

    Page<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId, Pageable pageable);
}
