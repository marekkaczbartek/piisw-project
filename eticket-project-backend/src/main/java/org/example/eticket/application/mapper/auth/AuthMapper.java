package org.example.eticket.application.mapper.auth;

import org.example.eticket.application.model.auth.AuthView;
import org.example.eticket.data.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    AuthView toView(User user, String token);
}

