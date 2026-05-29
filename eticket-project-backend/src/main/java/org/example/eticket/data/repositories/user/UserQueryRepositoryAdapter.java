package org.example.eticket.data.repositories.user;

import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.mapper.UserDataMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserQueryRepositoryAdapter implements UserQueryRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserDataMapper userDataMapper;

    public UserQueryRepositoryAdapter(UserJpaRepository userJpaRepository, UserDataMapper userDataMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userDataMapper = userDataMapper;
    }

    @Override
    public Optional<UserData> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userDataMapper::toData);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}

