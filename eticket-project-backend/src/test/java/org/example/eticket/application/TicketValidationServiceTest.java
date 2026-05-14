package org.example.eticket.application;

import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.service.TicketValidationService;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketValidationServiceTest {

    @Test
    void periodTicketIsValidWhenCheckedWithinValidity() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime expiresAt = boughtAt.plusDays(1);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                boughtAt.plusHours(3),
                "BUS-10"
        );

        assertTrue(service.isValid(command).valid());
    }

    @Test
    void periodTicketIsInvalidWhenCheckedAfterExpiry() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime expiresAt = boughtAt.plusDays(1);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                expiresAt.plusMinutes(1),
                "BUS-10"
        );

        assertFalse(service.isValid(command).valid());
    }

    @Test
    void singleUseTicketIsValidWhenPunchedInCheckedVehicle() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(5),
                "BUS-10"
        );

        assertTrue(service.isValid(command).valid());
    }

    @Test
    void singleUseTicketIsInvalidWhenCheckedInDifferentVehicle() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(5),
                "BUS-11"
        );

        assertFalse(service.isValid(command).valid());
    }

    @Test
    void timeBasedTicketIsValidWhenNotExpired() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .durationMinutes(30)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(20),
                "BUS-10"
        );

        assertTrue(service.isValid(command).valid());
    }

    @Test
    void timeBasedTicketIsInvalidAfterExpiry() {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .durationMinutes(30)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository(purchase));

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                expiresAt.plusMinutes(1),
                "BUS-10"
        );

        assertFalse(service.isValid(command).valid());
    }

    @Test
    void throwsWhenPurchaseDoesNotExist() {
        TicketValidationService service = new TicketValidationService(new InMemoryPurchaseQueryRepository());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.isValid(
                new ValidateTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private static Purchase purchase(
            Ticket ticket,
            UUID id,
            LocalDateTime boughtAt,
            LocalDateTime punchedAt,
            String punchedIn,
            LocalDateTime expiresAt
    ) {
        return Purchase.builder()
                .id(id)
                .ticket(ticket)
                .boughtAt(boughtAt)
                .punchedAt(punchedAt)
                .punchedIn(punchedIn)
                .expiresAt(expiresAt)
                .build();
    }

    private record InMemoryPurchaseQueryRepository(Map<UUID, Purchase> purchases) implements PurchaseQueryRepository {

        private InMemoryPurchaseQueryRepository(Purchase... purchases) {
            this(purchasesMap(purchases));
        }

        private static Map<UUID, Purchase> purchasesMap(Purchase... purchases) {
            Map<UUID, Purchase> map = new HashMap<>();
            for (Purchase purchase : purchases) {
                map.put(purchase.getId(), purchase);
            }
            return map;
        }

        @Override
        public Optional<Purchase> findById(UUID id) {
            return Optional.ofNullable(purchases.get(id));
        }
    }
}

