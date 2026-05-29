package org.example.eticket.application;

import org.example.eticket.application.exception.EmailAlreadyRegisteredException;
import org.example.eticket.application.mapper.auth.AuthMapper;
import org.example.eticket.application.model.auth.LoginCommand;
import org.example.eticket.application.model.auth.RegisterCommand;
import org.example.eticket.application.service.auth.AuthService;
import org.example.eticket.application.service.auth.JwtService;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.user.UserCommandRepository;
import org.example.eticket.data.repositories.user.UserQueryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mapstruct.factory.Mappers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registerCreatesUserWithEncodedPassword() {
        // given
        UserCommandRepository userCommandRepository = mock(UserCommandRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
        AuthService authService = new AuthService(
                userCommandRepository,
                userQueryRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                authMapper
        );

        RegisterCommand command = new RegisterCommand("new@example.com", "secret", "New", "User");

        when(userQueryRepository.existsByEmail(command.email())).thenReturn(false);
        when(jwtService.generateToken(any(UserData.class))).thenReturn("token");

        ArgumentCaptor<UserData> captor = ArgumentCaptor.forClass(UserData.class);
        when(userCommandRepository.save(captor.capture())).thenAnswer(invocation -> {
            UserData saved = invocation.getArgument(0, UserData.class);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // when
        authService.register(command);

        // then
        UserData captured = captor.getValue();
        assertEquals(command.email(), captured.getEmail());
        assertEquals(UserRole.PASSENGER, captured.getRole());
        assertTrue(passwordEncoder.matches(command.password(), captured.getPasswordHash()));
    }

    @Test
    void loginAuthenticatesAndReturnsUser() {
        // given
        UserCommandRepository userCommandRepository = mock(UserCommandRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
        AuthService authService = new AuthService(
                userCommandRepository,
                userQueryRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                authMapper
        );

        LoginCommand command = new LoginCommand("user@example.com", "secret");
        UserData user = UserData.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName("Ula")
                .lastName("User")
                .role(UserRole.PASSENGER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        when(userQueryRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        // when
        var result = authService.login(command);

        // then
        assertEquals(command.email(), result.email());
    }

    @Test
    void registerThrowsWhenEmailAlreadyRegistered() {
        // given
        UserCommandRepository userCommandRepository = mock(UserCommandRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
        AuthService authService = new AuthService(
                userCommandRepository,
                userQueryRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                authMapper
        );

        RegisterCommand command = new RegisterCommand("new@example.com", "secret", "New", "User");

        when(userQueryRepository.existsByEmail(command.email())).thenReturn(true);

        // when
        EmailAlreadyRegisteredException ex = assertThrows(EmailAlreadyRegisteredException.class,
                () -> authService.register(command));

        // then
        assertEquals("Email already registered", ex.getMessage());
    }
}
