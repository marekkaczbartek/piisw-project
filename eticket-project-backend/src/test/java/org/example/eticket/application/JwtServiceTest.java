package org.example.eticket.application;

import org.example.eticket.application.service.auth.JwtService;
import org.example.eticket.config.JwtProperties;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generatesAndValidatesToken() {
        // given
        JwtProperties properties = new JwtProperties(
                "wNn9n1wH2lQXHmpd2nq5W6JdVwT1r5xF3hP2a1p9nQw=",
                "eticket",
                60);

        JwtService jwtService = new JwtService(properties);
        UserData user = new UserData(
                UUID.randomUUID(),
                UserRole.PASSENGER,
                "user@example.com",
                "hash",
                "Ula",
                "User"
        );

        // when
        String token = jwtService.generateToken(user);

        // then
        assertEquals(user.email(), jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, user.email()));
    }

    @Test
    void rejectsTokenWithUnexpectedIssuer() {
        // given
        JwtProperties expectedIssuerProperties = new JwtProperties(
                "wNn9n1wH2lQXHmpd2nq5W6JdVwT1r5xF3hP2a1p9nQw=",
                "eticket",
                60);
        JwtProperties unexpectedIssuerProperties = new JwtProperties(
                "wNn9n1wH2lQXHmpd2nq5W6JdVwT1r5xF3hP2a1p9nQw=",
                "other-issuer",
                60);

        JwtService validatorService = new JwtService(expectedIssuerProperties);
        JwtService tokenIssuerService = new JwtService(unexpectedIssuerProperties);
        UserData user = new UserData(
                UUID.randomUUID(),
                UserRole.PASSENGER,
                "user@example.com",
                "hash",
                "Ula",
                "User"
        );

        // when
        String token = tokenIssuerService.generateToken(user);

        // then
        assertFalse(validatorService.isTokenValid(token, user.email()));
    }
}
