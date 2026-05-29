package org.example.eticket.data.mapper;

import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.entities.Purchase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserDataMapper.class, TicketDataMapper.class})
public interface PurchaseDataMapper {
    PurchaseData toData(Purchase purchase);

    Purchase toEntity(PurchaseData data);
}

