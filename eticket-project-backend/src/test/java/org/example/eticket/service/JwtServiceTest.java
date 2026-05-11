package org.example.eticket.service;

import org.example.eticket.config.JwtProperties;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generatesAndValidatesToken() {
        JwtProperties properties = new JwtProperties(
                "wNn9n1wH2lQXHmpd2nq5W6JdVwT1r5xF3hP2a1p9nQw=",
                "eticket",
                60);

        JwtService jwtService = new JwtService(properties);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("hash")
                .firstName("Ula")
                .lastName("User")
                .role(UserRole.PASSENGER)
                .build();

        String token = jwtService.generateToken(user);

        assertEquals(user.getEmail(), jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, user.getEmail()));
    }
}

