package org.example.eticket.data.repositories.user;

import org.example.eticket.data.entities.User;

import java.util.Optional;

public interface UserQueryRepository {
    Optional<User> findByEmail(String email);
}

