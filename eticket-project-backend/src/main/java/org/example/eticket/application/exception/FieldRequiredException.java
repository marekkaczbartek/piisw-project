package org.example.eticket.application.exception;

public class FieldRequiredException extends ApiException {

    public FieldRequiredException(String fieldName) {
        super(fieldName + " is required");
    }
}

