package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.mapper.PurchaseDataMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseCommandRepositoryAdapter implements PurchaseCommandRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;
    private final PurchaseDataMapper purchaseDataMapper;

    public PurchaseCommandRepositoryAdapter(PurchaseJpaRepository purchaseJpaRepository, PurchaseDataMapper purchaseDataMapper) {
        this.purchaseJpaRepository = purchaseJpaRepository;
        this.purchaseDataMapper = purchaseDataMapper;
    }

    @Override
    public PurchaseData save(PurchaseData purchase) {
        Purchase saved = purchaseJpaRepository.save(purchaseDataMapper.toEntity(purchase));
        return purchaseDataMapper.toData(saved);
    }
}

