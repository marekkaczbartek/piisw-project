package org.example.eticket.data.mapper;

import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDataMapper {
    UserData toData(User user);

    User toEntity(UserData data);
}

