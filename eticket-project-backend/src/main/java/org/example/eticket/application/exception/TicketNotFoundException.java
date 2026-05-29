package org.example.eticket.application.exception;

public class TicketNotFoundException extends ApiException {

    public TicketNotFoundException() {
        super("Ticket not found");
    }
}

