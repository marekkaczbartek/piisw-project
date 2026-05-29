package org.example.eticket.data.mapper;

import org.example.eticket.data.dto.ValidationData;
import org.example.eticket.data.entities.Validation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserDataMapper.class, PurchaseDataMapper.class})
public interface ValidationDataMapper {
    ValidationData toData(Validation validation);

    Validation toEntity(ValidationData data);
}

