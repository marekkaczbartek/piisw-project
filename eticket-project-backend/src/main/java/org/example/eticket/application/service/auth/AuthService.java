package org.example.eticket.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.ConflictException;
import org.example.eticket.application.exception.UnauthorizedException;
import org.example.eticket.application.model.auth.AuthView;
import org.example.eticket.application.model.auth.LoginCommand;
import org.example.eticket.application.model.auth.RegisterCommand;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.user.UserJpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private static AuthView toView(User user, String token) {
        return new AuthView(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public AuthView register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = userRepository.save(User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(UserRole.PASSENGER)
                .build());

        return toView(user, jwtService.generateToken(user));
    }

    public AuthView login(LoginCommand command) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return toView(user, jwtService.generateToken(user));
    }
}
