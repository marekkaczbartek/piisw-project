package org.example.eticket.application.model.auth;

public record RegisterCommand(String email, String password, String firstName, String lastName) {
}

