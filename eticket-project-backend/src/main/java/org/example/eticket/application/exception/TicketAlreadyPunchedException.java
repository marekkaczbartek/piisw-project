package org.example.eticket.application.exception;

public class TicketAlreadyPunchedException extends ApiException {

    public TicketAlreadyPunchedException() {
        super("Ticket already punched");
    }
}

