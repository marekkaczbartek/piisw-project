package org.example.eticket.data.repositories.purchase;

import org.example.eticket.data.dto.PurchaseData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseQueryRepository {

    Optional<PurchaseData> findById(UUID id);

    List<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId);

    Page<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId, Pageable pageable);
}
