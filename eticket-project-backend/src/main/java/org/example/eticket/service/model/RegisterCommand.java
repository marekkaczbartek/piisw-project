package org.example.eticket.service.model;

public record RegisterCommand(String email, String password, String firstName, String lastName) {
}

