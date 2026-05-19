package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId) {
        return purchaseJpaRepository.findAllByPassengerIdOrderByBoughtAtDesc(passengerId);
    }

    @Override
    public Page<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId, Pageable pageable) {
        return purchaseJpaRepository.findAllByPassengerIdOrderByBoughtAtDesc(passengerId, pageable);
    }
}
