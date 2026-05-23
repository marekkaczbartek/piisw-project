package org.example.eticket.application.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.UnauthorizedException;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResolver {

    private final UserQueryRepository userQueryRepository;

    public User resolveByEmail(String email, String notFoundMessage) {
        return userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(notFoundMessage));
    }
}
