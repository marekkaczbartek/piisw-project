package org.example.eticket.data.repositories.user;

import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.mapper.UserDataMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserCommandRepositoryAdapter implements UserCommandRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserDataMapper userDataMapper;

    public UserCommandRepositoryAdapter(UserJpaRepository userJpaRepository, UserDataMapper userDataMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userDataMapper = userDataMapper;
    }

    @Override
    public UserData save(UserData user) {
        User saved = userJpaRepository.save(userDataMapper.toEntity(user));
        return userDataMapper.toData(saved);
    }
}

