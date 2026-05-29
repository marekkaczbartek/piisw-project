package org.example.eticket.data.repositories.user;

import org.example.eticket.data.dto.UserData;

import java.util.Optional;

public interface UserQueryRepository {
    Optional<UserData> findByEmail(String email);

    boolean existsByEmail(String email);
}

