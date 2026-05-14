package org.example.eticket.api.dto.auth;

public record RegisterRequest(String email, String password, String firstName, String lastName) {
}

