package org.example.eticket.application.model.auth;

import org.example.eticket.data.enums.UserRole;

import java.util.UUID;

public record AuthView(String token, UUID id, String email, UserRole role, String firstName, String lastName) {
}

