package org.example.eticket.application;

import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.service.TicketValidationService;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.entities.Validation;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.example.eticket.data.repositories.ValidationCommandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketValidationServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
        User inspector = inspector("inspector@example.com");
        InMemoryValidationCommandRepository validationRepository = new InMemoryValidationCommandRepository();
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                validationRepository
        );
        setInspector(inspector.getEmail());

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                boughtAt.plusHours(3),
                "BUS-10"
        );

        assertTrue(service.isValid(command).valid());
        Validation saved = validationRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(inspector.getEmail(), saved.getInspector().getEmail());
        assertEquals(purchaseId, saved.getPurchase().getId());
        assertEquals(command.checkedAt(), saved.getCheckedAt());
        assertEquals(command.checkedIn(), saved.getCheckedIn());
        assertTrue(saved.getResult());
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
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

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
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

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
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

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
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

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
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                expiresAt.plusMinutes(1),
                "BUS-10"
        );

        assertFalse(service.isValid(command).valid());
    }

    @Test
    void throwsWhenPurchaseDoesNotExist() {
        User inspector = inspector("inspector@example.com");
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryValidationCommandRepository()
        );
        setInspector(inspector.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.isValid(
                new ValidateTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void throwsWhenUserIsNotInspector() {
        UUID purchaseId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, LocalDateTime.now(), "BUS-10", null);
        User passenger = User.builder()
                .id(UUID.randomUUID())
                .email("passenger@example.com")
                .role(UserRole.PASSENGER)
                .build();
        TicketValidationService service = new TicketValidationService(
                new InMemoryPurchaseQueryRepository(purchase),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryValidationCommandRepository()
        );
        setInspector(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.isValid(
                new ValidateTicketCommand(purchaseId, LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private static User inspector(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(UserRole.INSPECTOR)
                .build();
    }

    private static void setInspector(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of())
        );
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

    private record InMemoryUserQueryRepository(Map<String, User> users) implements UserQueryRepository {

        private InMemoryUserQueryRepository(User... users) {
            this(usersMap(users));
        }

        private static Map<String, User> usersMap(User... users) {
            Map<String, User> map = new HashMap<>();
            for (User user : users) {
                map.put(user.getEmail(), user);
            }
            return map;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(users.get(email));
        }
    }

    private static class InMemoryValidationCommandRepository implements ValidationCommandRepository {

        private final List<Validation> saved = new ArrayList<>();

        @Override
        public Validation save(Validation validation) {
            saved.add(validation);
            return validation;
        }

        private Validation singleSaved() {
            if (saved.isEmpty()) {
                return null;
            }
            return saved.get(0);
        }
    }
}
