package org.example.eticket.application.mapper.auth;

import org.example.eticket.application.model.auth.AuthView;
import org.example.eticket.data.dto.UserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    AuthView toView(UserData user, String token);
}

