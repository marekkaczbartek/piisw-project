package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserQueryRepositoryAdapter implements UserQueryRepository {

    private final UserJpaRepository userJpaRepository;

    public UserQueryRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }
}

