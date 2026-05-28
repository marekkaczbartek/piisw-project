package org.example.eticket.data.repositories.user;

import org.example.eticket.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    java.util.Optional<User> findByEmail(String email);
}
