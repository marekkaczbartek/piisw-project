package org.example.eticket.application;

import org.example.eticket.application.exception.NotFoundException;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.application.service.validation.ValidationService;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.entities.Validation;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.purchase.PurchaseCommandRepository;
import org.example.eticket.data.repositories.purchase.PurchaseQueryRepository;
import org.example.eticket.data.repositories.user.UserQueryRepository;
import org.example.eticket.data.repositories.validation.ValidationCommandRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TicketValidationServiceTest {

    @Test
    void periodTicketIsTicketValidWhenCheckedWithinValidity() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime expiresAt = boughtAt.plusDays(1);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        InMemoryValidationCommandRepository validationRepository = new InMemoryValidationCommandRepository();
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                validationRepository,
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                boughtAt.plusHours(3),
                "BUS-10"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertTrue(result.valid());
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
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime expiresAt = boughtAt.plusDays(1);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                expiresAt.plusMinutes(1),
                "BUS-10"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertFalse(result.valid());
    }

    @Test
    void singleUseTicketIsTicketValidWhenPunchedInCheckedVehicle() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(5),
                "BUS-10"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertTrue(result.valid());
    }

    @Test
    void singleUseTicketIsInvalidAfterBeingValidatedOnce() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand firstCommand = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(5),
                "BUS-10"
        );
        ValidateTicketCommand secondCommand = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(6),
                "BUS-10"
        );

        // when
        var firstResult = service.validatePurchase(firstCommand, inspector.getEmail());
        var secondResult = service.validatePurchase(secondCommand, inspector.getEmail());

        // then
        assertTrue(firstResult.valid());
        assertEquals(firstCommand.checkedAt(), purchase.getExpiresAt());
        assertFalse(secondResult.valid());
    }

    @Test
    void singleUseTicketIsInvalidWhenCheckedInDifferentVehicle() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(5),
                "BUS-11"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertFalse(result.valid());
    }

    @Test
    void timeBasedTicketIsTicketValidWhenNotExpired() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .durationMinutes(30)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                punchedAt.plusMinutes(20),
                "BUS-10"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertTrue(result.valid());
    }

    @Test
    void timeBasedTicketIsInvalidAfterExpiry() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .durationMinutes(30)
                .build();
        Purchase purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        Map<UUID, Purchase> purchases = purchasesMap(purchase);
        User inspector = inspector("inspector@example.com");
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(
                purchaseId,
                expiresAt.plusMinutes(1),
                "BUS-10"
        );

        // when
        var result = service.validatePurchase(command, inspector.getEmail());

        // then
        assertFalse(result.valid());
    }

    @Test
    void throwsWhenPurchaseDoesNotExist() {
        // given
        User inspector = inspector("inspector@example.com");
        Map<UUID, Purchase> purchases = purchasesMap();
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10");

        // when
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.validatePurchase(
                command,
                inspector.getEmail()
        ));

        // then
        assertEquals("Purchase not found", ex.getMessage());
    }

    private static User inspector(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(UserRole.INSPECTOR)
                .build();
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

    private static Map<UUID, Purchase> purchasesMap(Purchase... purchases) {
        Map<UUID, Purchase> map = new HashMap<>();
        for (Purchase purchase : purchases) {
            map.put(purchase.getId(), purchase);
        }
        return map;
    }

    private record InMemoryPurchaseQueryRepository(Map<UUID, Purchase> purchases) implements PurchaseQueryRepository {

        private InMemoryPurchaseQueryRepository(Purchase... purchases) {
            this(purchasesMap(purchases));
        }

        @Override
        public Optional<Purchase> findById(UUID id) {
            return Optional.ofNullable(purchases.get(id));
        }

        @Override
        public List<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId) {
            return purchases.values().stream()
                    .filter(purchase -> purchase.getPassenger() != null)
                    .filter(purchase -> passengerId.equals(purchase.getPassenger().getId()))
                    .sorted(Comparator.comparing(Purchase::getBoughtAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .toList();
        }

        @Override
        public org.springframework.data.domain.Page<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(
                UUID passengerId,
                org.springframework.data.domain.Pageable pageable
        ) {
            List<Purchase> sorted = findAllByPassengerIdOrderByBoughtAtDesc(passengerId);
            int start = Math.toIntExact(pageable.getOffset());
            if (start >= sorted.size()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, sorted.size());
            }
            int end = Math.min(start + pageable.getPageSize(), sorted.size());
            return new org.springframework.data.domain.PageImpl<>(sorted.subList(start, end), pageable, sorted.size());
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
            return saved.getFirst();
        }
    }

    private static class InMemoryPurchaseCommandRepository implements PurchaseCommandRepository {

        private final Map<UUID, Purchase> purchases;

        private InMemoryPurchaseCommandRepository(Map<UUID, Purchase> purchases) {
            this.purchases = purchases;
        }

        @Override
        public Purchase save(Purchase purchase) {
            purchases.put(purchase.getId(), purchase);
            return purchase;
        }
    }
}
