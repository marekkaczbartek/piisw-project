package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.entities.Purchase;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseCommandRepositoryAdapter implements PurchaseCommandRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;

    public PurchaseCommandRepositoryAdapter(PurchaseJpaRepository purchaseJpaRepository) {
        this.purchaseJpaRepository = purchaseJpaRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        return purchaseJpaRepository.save(purchase);
    }
}

