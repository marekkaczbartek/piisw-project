package org.example.eticket.service;

import org.example.eticket.api.dto.LoginRequest;
import org.example.eticket.api.dto.RegisterRequest;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.UserJpaRepository;
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

        RegisterRequest request = new RegisterRequest("new@example.com", "secret", "New", "User");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0, User.class);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        authService.register(request);

        User captured = captor.getValue();
        assertEquals(request.email(), captured.getEmail());
        assertEquals(UserRole.PASSENGER, captured.getRole());
        assertTrue(passwordEncoder.matches(request.password(), captured.getPasswordHash()));
    }

    @Test
    void loginAuthenticatesAndReturnsUser() {
        UserJpaRepository userRepository = mock(UserJpaRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);

        LoginRequest request = new LoginRequest("user@example.com", "secret");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName("Ula")
                .lastName("User")
                .role(UserRole.PASSENGER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        assertEquals(request.email(), authService.login(request).email());
    }
}
