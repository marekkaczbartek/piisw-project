package org.example.eticket.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.eticket.data.enums.UserRole;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(String token, UUID id, String email, UserRole role, String firstName, String lastName) {
}
