package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserResolver {

    private final UserQueryRepository userQueryRepository;

    public User resolveByEmail(String email, String notFoundMessage) {
        return userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, notFoundMessage));
    }
}

