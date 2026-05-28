package org.example.eticket.api.mapper.validation;

import org.example.eticket.api.dto.validation.TicketValidationResponse;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidationResponseMapper {

    TicketValidationResponse toResponse(ValidationResultView view);
}

