package org.example.eticket.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.UnauthorizedException;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.repositories.user.UserQueryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResolver {

    private final UserQueryRepository userQueryRepository;

    public UserData resolveByEmail(String email, String notFoundMessage) {
        return userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(notFoundMessage));
    }
}
