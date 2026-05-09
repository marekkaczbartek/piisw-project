package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, UUID> {
}

