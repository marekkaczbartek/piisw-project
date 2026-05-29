package org.example.eticket.data.repositories.user;

import org.example.eticket.data.dto.UserData;

public interface UserCommandRepository {
    UserData save(UserData user);
}

