package org.example.eticket.data.dto;

import org.example.eticket.data.enums.UserRole;

import java.util.UUID;

public record UserData(
        UUID id,
        UserRole role,
        String email,
        String passwordHash,
        String firstName,
        String lastName
) {
}

