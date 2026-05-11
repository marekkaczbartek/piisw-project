package org.example.eticket.service;

import lombok.RequiredArgsConstructor;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.UserJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.example.eticket.service.model.AuthView;
import org.example.eticket.service.model.LoginCommand;
import org.example.eticket.service.model.RegisterCommand;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        return toView(user, jwtService.generateToken(user));
    }
}
