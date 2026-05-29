package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.mapper.PurchaseDataMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PurchaseQueryRepositoryAdapter implements PurchaseQueryRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;
    private final PurchaseDataMapper purchaseDataMapper;

    public PurchaseQueryRepositoryAdapter(PurchaseJpaRepository purchaseJpaRepository, PurchaseDataMapper purchaseDataMapper) {
        this.purchaseJpaRepository = purchaseJpaRepository;
        this.purchaseDataMapper = purchaseDataMapper;
    }

    @Override
    public Optional<PurchaseData> findById(UUID id) {
        return purchaseJpaRepository.findById(id)
                .map(purchaseDataMapper::toData);
    }

    @Override
    public List<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId) {
        return purchaseJpaRepository.findAllByPassengerIdOrderByBoughtAtDesc(passengerId).stream()
                .map(purchaseDataMapper::toData)
                .toList();
    }

    @Override
    public Page<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId, Pageable pageable) {
        return purchaseJpaRepository.findAllByPassengerIdOrderByBoughtAtDesc(passengerId, pageable)
                .map(purchaseDataMapper::toData);
    }
}
