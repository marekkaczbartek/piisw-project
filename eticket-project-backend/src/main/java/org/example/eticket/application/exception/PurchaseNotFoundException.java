package org.example.eticket.application.exception;

public class PurchaseNotFoundException extends ApiException {

    public PurchaseNotFoundException() {
        super("Purchase not found");
    }
}

