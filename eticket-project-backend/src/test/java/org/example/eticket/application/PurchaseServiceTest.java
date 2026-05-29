package org.example.eticket.application;

import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.exception.PurchaseNotFoundException;
import org.example.eticket.application.exception.TicketAlreadyPunchedException;
import org.example.eticket.application.exception.TicketNotFoundException;
import org.example.eticket.application.mapper.purchase.PurchaseMapper;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PunchTicketCommand;
import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.application.service.purchase.PurchaseService;
import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.dto.TicketData;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.purchase.PurchaseCommandRepository;
import org.example.eticket.data.repositories.purchase.PurchaseQueryRepository;
import org.example.eticket.data.repositories.ticket.TicketQueryRepository;
import org.example.eticket.data.repositories.user.UserQueryRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseServiceTest {

    @Test
    void createsSingleUsePurchaseWithoutExpiry() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                DiscountType.NORMAL,
                new BigDecimal("3.50"),
                null
        );
        UserData passenger = passenger("passenger@example.com");
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(List.of()),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);

        // when
        PurchaseView view = service.makePurchase(new MakePurchaseCommand(
                ticket.ticketType(),
                ticket.discountType(),
                ticket.durationMinutes(),
                boughtAt
        ), passenger.email());

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(passenger.email(), saved.passenger().email());
        assertEquals(ticket.ticketType(), saved.ticket().ticketType());
        assertEquals(boughtAt, saved.boughtAt());
        assertNull(saved.punchedAt());
        assertNull(saved.punchedIn());
        assertNull(saved.expiresAt());
        assertEquals(saved.id(), view.id());
        assertEquals(ticket.ticketType(), view.ticketType());
        assertEquals(ticket.discountType(), view.discountType());
        assertEquals(ticket.price(), view.price());
        assertEquals(ticket.durationMinutes(), view.durationMinutes());
        assertEquals(boughtAt, view.boughtAt());
        assertNull(view.expiresAt());
    }

    @Test
    void createsPeriodPurchaseWithExpiryAtPurchaseTime() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.PERIOD,
                DiscountType.REDUCED,
                new BigDecimal("12.00"),
                24 * 60
        );
        UserData passenger = passenger("passenger@example.com");
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(List.of()),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);

        // when
        PurchaseView view = service.makePurchase(new MakePurchaseCommand(
                ticket.ticketType(),
                ticket.discountType(),
                ticket.durationMinutes(),
                boughtAt
        ), passenger.email());

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(boughtAt.plusMinutes(ticket.durationMinutes()), saved.expiresAt());
        assertEquals(boughtAt.plusMinutes(ticket.durationMinutes()), view.expiresAt());
    }

    @Test
    void throwsWhenTicketDoesNotExist() {
        // given
        UserData passenger = passenger("passenger@example.com");
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(List.of()),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        TicketNotFoundException ex = assertThrows(TicketNotFoundException.class, () -> service.makePurchase(
                new MakePurchaseCommand(TicketType.SINGLE_USE, DiscountType.NORMAL, null, LocalDateTime.now()),
                passenger.email()
        ));

        // then
        assertEquals("Ticket not found", ex.getMessage());
    }

    @Test
    void punchesSingleUseTicket() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                DiscountType.NORMAL,
                new BigDecimal("3.50"),
                null
        );
        UserData passenger = passenger("passenger@example.com");
        PurchaseData purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);

        // when
        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.id(),
                punchedAt,
                "BUS-10"
        ));

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(punchedAt, saved.punchedAt());
        assertEquals("BUS-10", saved.punchedIn());
        assertNull(saved.expiresAt());
        assertEquals(purchase.id(), view.id());
        assertEquals(punchedAt, view.punchedAt());
        assertEquals("BUS-10", view.punchedIn());
        assertNull(view.expiresAt());
    }

    @Test
    void punchesTimeBasedTicketWithExpiry() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.TIME_BASED,
                DiscountType.REDUCED,
                new BigDecimal("2.00"),
                30
        );
        UserData passenger = passenger("passenger@example.com");
        PurchaseData purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);

        // when
        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.id(),
                punchedAt,
                "TRAM-2"
        ));

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertEquals(punchedAt.plusMinutes(30), saved.expiresAt());
        assertEquals(punchedAt.plusMinutes(30), view.expiresAt());
    }

    @Test
    void throwsWhenPunchPurchaseDoesNotExist() {
        // given
        UserData passenger = passenger("passenger@example.com");
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        PurchaseNotFoundException ex = assertThrows(PurchaseNotFoundException.class, () -> service.punchTicket(
                new PunchTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        // then
        assertEquals("Purchase not found", ex.getMessage());
    }

    @Test
    void punchesTicketWithoutPassengerOwnershipCheck() {
        // given
        UserData owner = passenger("owner@example.com");
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                null,
                null,
                null
        );
        PurchaseData purchase = purchase(UUID.randomUUID(), owner, ticket, LocalDateTime.now());
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(owner))
        );

        // when
        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.id(),
                LocalDateTime.now(),
                "BUS-10"
        ));

        // then
        assertEquals(purchase.id(), view.id());
    }

    @Test
    void throwsWhenPunchTicketAlreadyPunched() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.SINGLE_USE,
                null,
                null,
                null
        );
        UserData passenger = passenger("passenger@example.com");
        PurchaseData purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        PurchaseData punchedPurchase = new PurchaseData(
                purchase.id(),
                purchase.passenger(),
                purchase.ticket(),
                purchase.boughtAt(),
                LocalDateTime.of(2024, 5, 10, 9, 0),
                purchase.punchedIn(),
                purchase.expiresAt()
        );
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(punchedPurchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        TicketAlreadyPunchedException ex = assertThrows(TicketAlreadyPunchedException.class, () -> service.punchTicket(
                new PunchTicketCommand(punchedPurchase.id(), LocalDateTime.of(2024, 5, 10, 10, 0), "BUS-10")
        ));

        // then
        assertEquals("Ticket already punched", ex.getMessage());
    }

    @Test
    void throwsWhenPunchPeriodTicket() {
        // given
        TicketData ticket = new TicketData(
                null,
                TicketType.PERIOD,
                null,
                null,
                24 * 60
        );
        UserData passenger = passenger("passenger@example.com");
        PurchaseData purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        PeriodTicketPunchNotAllowedException ex = assertThrows(PeriodTicketPunchNotAllowedException.class,
                () -> service.punchTicket(new PunchTicketCommand(purchase.id(), LocalDateTime.now(), "BUS-10"))
        );

        // then
        assertEquals("Period tickets do not require punching", ex.getMessage());
    }

    private static UserData passenger(String email) {
        return new UserData(
                java.util.UUID.randomUUID(),
                UserRole.PASSENGER,
                email,
                null,
                null,
                null
        );
    }


    private static PurchaseData purchase(UUID id, UserData passenger, TicketData ticket, LocalDateTime boughtAt) {
        return new PurchaseData(
                id,
                passenger,
                ticket,
                boughtAt,
                null,
                null,
                null
        );
    }

    private static PurchaseService purchaseService(
            TicketQueryRepository ticketQueryRepository,
            PurchaseCommandRepository purchaseCommandRepository,
            PurchaseQueryRepository purchaseQueryRepository,
            UserResolver userResolver
    ) {
        return new PurchaseService(
                ticketQueryRepository,
                purchaseCommandRepository,
                purchaseQueryRepository,
                userResolver,
                Mappers.getMapper(PurchaseMapper.class)
        );
    }

    private record InMemoryTicketQueryRepository(List<TicketData> tickets) implements TicketQueryRepository {

        private InMemoryTicketQueryRepository(TicketData... tickets) {
            this(List.of(tickets));
        }

        @Override
        public List<TicketData> findAll() {
            return List.copyOf(tickets);
        }

        @Override
        public Optional<TicketData> findByTicketTypeAndDiscountTypeAndDurationMinutes(
                TicketType ticketType,
                DiscountType discountType,
                Integer durationMinutes
        ) {
            return tickets.stream()
                    .filter(ticket -> ticketType == ticket.ticketType())
                    .filter(ticket -> discountType == ticket.discountType())
                    .filter(ticket -> durationMinutes == null
                            ? ticket.durationMinutes() == null
                            : durationMinutes.equals(ticket.durationMinutes()))
                    .findFirst();
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

    private static class InMemoryPurchaseCommandRepository implements PurchaseCommandRepository {

        private final List<PurchaseData> saved = new ArrayList<>();

        @Override
        public PurchaseData save(PurchaseData purchase) {
            PurchaseData stored = purchase;
            if (purchase.id() == null) {
                stored = new PurchaseData(
                        java.util.UUID.randomUUID(),
                        purchase.passenger(),
                        purchase.ticket(),
                        purchase.boughtAt(),
                        purchase.punchedAt(),
                        purchase.punchedIn(),
                        purchase.expiresAt()
                );
            }
            saved.add(stored);
            return stored;
        }

        private PurchaseData singleSaved() {
            if (saved.isEmpty()) {
                return null;
            }
            return saved.getFirst();
        }
    }

    private record InMemoryPurchaseQueryRepository(List<PurchaseData> purchases) implements PurchaseQueryRepository {

        private InMemoryPurchaseQueryRepository(PurchaseData... purchases) {
            this(List.of(purchases));
        }

        private InMemoryPurchaseQueryRepository() {
            this(List.of());
        }

        @Override
        public Optional<PurchaseData> findById(java.util.UUID id) {
            return purchases.stream()
                    .filter(purchase -> id.equals(purchase.id()))
                    .findFirst();
        }

        @Override
        public List<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(java.util.UUID passengerId) {
            return List.copyOf(purchases);
        }

        @Override
        public org.springframework.data.domain.Page<PurchaseData> findAllByPassengerIdOrderByBoughtAtDesc(
                java.util.UUID passengerId,
                org.springframework.data.domain.Pageable pageable
        ) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }
    }
}
