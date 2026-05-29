package org.example.eticket.application.exception;

public class EmailAlreadyRegisteredException extends ApiException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered");
    }
}

