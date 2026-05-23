package org.example.eticket.api.exception;

import org.example.eticket.application.exception.BadRequestException;
import org.example.eticket.application.exception.ConflictException;
import org.example.eticket.application.exception.FieldRequiredException;
import org.example.eticket.application.exception.NotFoundException;
import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.exception.TicketAlreadyPunchedException;
import org.example.eticket.application.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(TicketAlreadyPunchedException.class)
    public ResponseEntity<ErrorResponse> handleTicketAlreadyPunched(TicketAlreadyPunchedException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PeriodTicketPunchNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handlePeriodTicketPunchNotAllowed(PeriodTicketPunchNotAllowedException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(FieldRequiredException.class)
    public ResponseEntity<ErrorResponse> handleFieldRequired(FieldRequiredException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message));
    }
}
