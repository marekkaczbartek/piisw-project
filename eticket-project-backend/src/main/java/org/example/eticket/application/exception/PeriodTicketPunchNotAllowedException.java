package org.example.eticket.application.exception;

public class PeriodTicketPunchNotAllowedException extends ApiException {

    public PeriodTicketPunchNotAllowedException() {
        super("Period tickets do not require punching");
    }
}

