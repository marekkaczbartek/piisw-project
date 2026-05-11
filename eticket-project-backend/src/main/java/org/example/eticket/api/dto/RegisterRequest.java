package org.example.eticket.api.dto;

public record RegisterRequest(String email, String password, String firstName, String lastName) {
}

