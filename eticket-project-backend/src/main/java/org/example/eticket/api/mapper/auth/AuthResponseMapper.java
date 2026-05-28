package org.example.eticket.api.mapper.auth;

import org.example.eticket.api.dto.auth.AuthResponse;
import org.example.eticket.application.model.auth.AuthView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthResponseMapper {

    AuthResponse toResponse(AuthView view);
}

