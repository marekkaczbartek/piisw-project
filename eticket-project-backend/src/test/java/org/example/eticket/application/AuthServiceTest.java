package org.example.eticket.application;

import org.example.eticket.application.model.auth.LoginCommand;
import org.example.eticket.application.model.auth.RegisterCommand;
import org.example.eticket.application.service.auth.AuthService;
import org.example.eticket.application.service.auth.JwtService;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.user.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registerCreatesUserWithEncodedPassword() {
        UserJpaRepository userRepository = mock(UserJpaRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);

        RegisterCommand command = new RegisterCommand("new@example.com", "secret", "New", "User");

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0, User.class);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        authService.register(command);

        User captured = captor.getValue();
        assertEquals(command.email(), captured.getEmail());
        assertEquals(UserRole.PASSENGER, captured.getRole());
        assertTrue(passwordEncoder.matches(command.password(), captured.getPasswordHash()));
    }

    @Test
    void loginAuthenticatesAndReturnsUser() {
        UserJpaRepository userRepository = mock(UserJpaRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);

        LoginCommand command = new LoginCommand("user@example.com", "secret");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName("Ula")
                .lastName("User")
                .role(UserRole.PASSENGER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        assertEquals(command.email(), authService.login(command).email());
    }
}
