package org.example.eticket.application;

import org.example.eticket.application.exception.PurchaseNotFoundException;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.application.service.validation.ValidationService;
import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.dto.ValidationData;
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
        TicketData ticket = new TicketData(
                null,
                TicketType.PERIOD,
                null,
                null,
                24 * 60
        );
        PurchaseData purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertTrue(result.valid());
        ValidationData saved = validationRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(inspector.email(), saved.inspector().email());
        assertEquals(purchaseId, saved.purchase().id());
        assertEquals(command.checkedAt(), saved.checkedAt());
        assertEquals(command.checkedIn(), saved.checkedIn());
        assertTrue(saved.result());
    }

    @Test
    void periodTicketIsInvalidWhenCheckedAfterExpiry() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime expiresAt = boughtAt.plusDays(1);
        TicketData ticket = new TicketData(
                null,
                TicketType.PERIOD,
                null,
                null,
                24 * 60
        );
        PurchaseData purchase = purchase(ticket, purchaseId, boughtAt, null, null, expiresAt);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertFalse(result.valid());
    }

    @Test
    void singleUseTicketIsTicketValidWhenPunchedInCheckedVehicle() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                null,
                null,
                null
        );
        PurchaseData purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertTrue(result.valid());
    }

    @Test
    void singleUseTicketIsInvalidAfterBeingValidatedOnce() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                null,
                null,
                null
        );
        PurchaseData purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var firstResult = service.validatePurchase(firstCommand, inspector.email());
        var secondResult = service.validatePurchase(secondCommand, inspector.email());

        // then
        assertTrue(firstResult.valid());
        assertEquals(firstCommand.checkedAt(), purchases.get(purchaseId).expiresAt());
        assertFalse(secondResult.valid());
    }

    @Test
    void singleUseTicketIsInvalidWhenCheckedInDifferentVehicle() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                null,
                null,
                null
        );
        PurchaseData purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", null);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertFalse(result.valid());
    }

    @Test
    void timeBasedTicketIsTicketValidWhenNotExpired() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        TicketData ticket = new TicketData(
                null,
                TicketType.TIME_BASED,
                null,
                null,
                30
        );
        PurchaseData purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertTrue(result.valid());
    }

    @Test
    void timeBasedTicketIsInvalidAfterExpiry() {
        // given
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = punchedAt.plusMinutes(30);
        TicketData ticket = new TicketData(
                null,
                TicketType.TIME_BASED,
                null,
                null,
                30
        );
        PurchaseData purchase = purchase(ticket, purchaseId, null, punchedAt, "BUS-10", expiresAt);
        Map<UUID, PurchaseData> purchases = purchasesMap(purchase);
        UserData inspector = inspector("inspector@example.com");
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
        var result = service.validatePurchase(command, inspector.email());

        // then
        assertFalse(result.valid());
    }

    @Test
    void throwsWhenPurchaseDoesNotExist() {
        // given
        UserData inspector = inspector("inspector@example.com");
        Map<UUID, PurchaseData> purchases = purchasesMap();
        ValidationService service = new ValidationService(
                new InMemoryPurchaseQueryRepository(purchases),
                new InMemoryPurchaseCommandRepository(purchases),
                new InMemoryValidationCommandRepository(),
                new UserResolver(new InMemoryUserQueryRepository(inspector))
        );
        ValidateTicketCommand command = new ValidateTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10");

        // when
        PurchaseNotFoundException ex = assertThrows(PurchaseNotFoundException.class, () -> service.validatePurchase(
                command,
                inspector.email()
        ));

        // then
        assertEquals("Purchase not found", ex.getMessage());
    }

    private static UserData inspector(String email) {
        return new UserData(
                UUID.randomUUID(),
                UserRole.INSPECTOR,
                email,
                null,
                null,
                null
        );
    }


    private static PurchaseData purchase(
            TicketData ticket,
            UUID id,
            LocalDateTime boughtAt,
            LocalDateTime punchedAt,
            String punchedIn,
            LocalDateTime expiresAt
    ) {
        return new PurchaseData(
                id,
                null,
                ticket,
                boughtAt,
                punchedAt,
                punchedIn,
                expiresAt
        );
    }

    private static Map<UUID, PurchaseData> purchasesMap(PurchaseData... purchases) {
        Map<UUID, PurchaseData> map = new HashMap<>();
        for (PurchaseData purchase : purchases) {
            map.put(purchase.id(), purchase);
        }
        return map;
    }

    private record InMemoryPurchaseQueryRepository(Map<UUID, PurchaseData> purchases) implements PurchaseQueryRepository {

        @Override
        public Optional<PurchaseData> findById(UUID id) {
            return Optional.ofNullable(purchases.get(id));
        }

        @Override
        public List<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(UUID passengerId) {
            return purchases.values().stream()
                    .filter(purchase -> purchase.passenger() != null)
                    .filter(purchase -> passengerId.equals(purchase.passenger().id()))
                    .sorted(Comparator.comparing(PurchaseData::boughtAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .toList();
        }

        @Override
        public org.springframework.data.domain.Page<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(
                UUID passengerId,
                org.springframework.data.domain.Pageable pageable
        ) {
            List<PurchaseData> sorted = findAllByPassengerIdOrderByBoughtAtDesc(passengerId);
            int start = Math.toIntExact(pageable.getOffset());
            if (start >= sorted.size()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, sorted.size());
            }
            int end = Math.min(start + pageable.getPageSize(), sorted.size());
            return new org.springframework.data.domain.PageImpl<>(sorted.subList(start, end), pageable, sorted.size());
        }
    }


    private record InMemoryUserQueryRepository(Map<String, UserData> users) implements UserQueryRepository {

        private InMemoryUserQueryRepository(UserData... users) {
            this(usersMap(users));
        }

        private static Map<String, UserData> usersMap(UserData... users) {
            Map<String, UserData> map = new HashMap<>();
            for (UserData user : users) {
                map.put(user.email(), user);
            }
            return map;
        }

        @Override
        public Optional<UserData> findByEmail(String email) {
            return Optional.ofNullable(users.get(email));
        }

        @Override
        public boolean existsByEmail(String email) {
            return users.containsKey(email);
        }
    }

    private static class InMemoryValidationCommandRepository implements ValidationCommandRepository {

        private final List<ValidationData> saved = new ArrayList<>();

        @Override
        public ValidationData save(ValidationData validation) {
            saved.add(validation);
            return validation;
        }

        private ValidationData singleSaved() {
            if (saved.isEmpty()) {
                return null;
            }
            return saved.getFirst();
        }
    }

    private record InMemoryPurchaseCommandRepository(
            Map<UUID, PurchaseData> purchases) implements PurchaseCommandRepository {

        @Override
        public PurchaseData save(PurchaseData purchase) {
            purchases.put(purchase.id(), purchase);
            return purchase;
        }
    }
}
