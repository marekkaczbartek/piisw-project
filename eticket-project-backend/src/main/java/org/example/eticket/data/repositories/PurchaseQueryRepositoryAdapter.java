package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Purchase;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PurchaseQueryRepositoryAdapter implements PurchaseQueryRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;

    public PurchaseQueryRepositoryAdapter(PurchaseJpaRepository purchaseJpaRepository) {
        this.purchaseJpaRepository = purchaseJpaRepository;
    }

    @Override
    public Optional<Purchase> findById(UUID id) {
        return purchaseJpaRepository.findById(id);
    }
}

