package org.example.eticket.application;

import org.example.eticket.application.exception.NotFoundException;
import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.exception.TicketAlreadyPunchedException;
import org.example.eticket.application.mapper.purchase.PurchaseMapper;
import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PunchTicketCommand;
import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.application.service.purchase.PurchaseService;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
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
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .durationMinutes(null)
                .build();
        User passenger = passenger("passenger@example.com");
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
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ), passenger.getEmail());

        // then
        Purchase saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(passenger.getEmail(), saved.getPassenger().getEmail());
        assertEquals(ticket.getTicketType(), saved.getTicket().getTicketType());
        assertEquals(boughtAt, saved.getBoughtAt());
        assertNull(saved.getPunchedAt());
        assertNull(saved.getPunchedIn());
        assertNull(saved.getExpiresAt());
        assertEquals(saved.getId(), view.id());
        assertEquals(ticket.getTicketType(), view.ticketType());
        assertEquals(ticket.getDiscountType(), view.discountType());
        assertEquals(ticket.getPrice(), view.price());
        assertEquals(ticket.getDurationMinutes(), view.durationMinutes());
        assertEquals(boughtAt, view.boughtAt());
        assertNull(view.expiresAt());
    }

    @Test
    void createsPeriodPurchaseWithExpiryAtPurchaseTime() {
        // given
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("12.00"))
                .durationMinutes(24 * 60)
                .build();
        User passenger = passenger("passenger@example.com");
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
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ), passenger.getEmail());

        // then
        Purchase saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), saved.getExpiresAt());
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), view.expiresAt());
    }

    @Test
    void throwsWhenTicketDoesNotExist() {
        // given
        User passenger = passenger("passenger@example.com");
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(List.of()),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.makePurchase(
                new MakePurchaseCommand(TicketType.SINGLE_USE, DiscountType.NORMAL, null, LocalDateTime.now()),
                passenger.getEmail()
        ));

        // then
        assertEquals("Ticket not found", ex.getMessage());
    }

    @Test
    void punchesSingleUseTicket() {
        // given
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
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
                purchase.getId(),
                punchedAt,
                "BUS-10"
        ));

        // then
        Purchase saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(punchedAt, saved.getPunchedAt());
        assertEquals("BUS-10", saved.getPunchedIn());
        assertNull(saved.getExpiresAt());
        assertEquals(purchase.getId(), view.id());
        assertEquals(punchedAt, view.punchedAt());
        assertEquals("BUS-10", view.punchedIn());
        assertNull(view.expiresAt());
    }

    @Test
    void punchesTimeBasedTicketWithExpiry() {
        // given
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("2.00"))
                .durationMinutes(30)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
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
                purchase.getId(),
                punchedAt,
                "TRAM-2"
        ));

        // then
        Purchase saved = purchaseRepository.singleSaved();
        assertEquals(punchedAt.plusMinutes(30), saved.getExpiresAt());
        assertEquals(punchedAt.plusMinutes(30), view.expiresAt());
    }

    @Test
    void throwsWhenPunchPurchaseDoesNotExist() {
        // given
        User passenger = passenger("passenger@example.com");
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.punchTicket(
                new PunchTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        // then
        assertEquals("Purchase not found", ex.getMessage());
    }

    @Test
    void punchesTicketWithoutPassengerOwnershipCheck() {
        // given
        User owner = passenger("owner@example.com");
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(UUID.randomUUID(), owner, ticket, LocalDateTime.now());
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(owner))
        );

        // when
        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.getId(),
                LocalDateTime.now(),
                "BUS-10"
        ));

        // then
        assertEquals(purchase.getId(), view.id());
    }

    @Test
    void throwsWhenPunchTicketAlreadyPunched() {
        // given
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        purchase.setPunchedAt(LocalDateTime.of(2024, 5, 10, 9, 0));
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        TicketAlreadyPunchedException ex = assertThrows(TicketAlreadyPunchedException.class, () -> service.punchTicket(
                new PunchTicketCommand(purchase.getId(), LocalDateTime.of(2024, 5, 10, 10, 0), "BUS-10")
        ));

        // then
        assertEquals("Ticket already punched", ex.getMessage());
    }

    @Test
    void throwsWhenPunchPeriodTicket() {
        // given
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        PurchaseService service = purchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase),
                new UserResolver(new InMemoryUserQueryRepository(passenger))
        );

        // when
        PeriodTicketPunchNotAllowedException ex = assertThrows(PeriodTicketPunchNotAllowedException.class,
                () -> service.punchTicket(new PunchTicketCommand(purchase.getId(), LocalDateTime.now(), "BUS-10"))
        );

        // then
        assertEquals("Period tickets do not require punching", ex.getMessage());
    }

    private static User passenger(String email) {
        return User.builder()
                .id(java.util.UUID.randomUUID())
                .email(email)
                .role(UserRole.PASSENGER)
                .build();
    }


    private static Purchase purchase(UUID id, User passenger, Ticket ticket, LocalDateTime boughtAt) {
        return Purchase.builder()
                .id(id)
                .passenger(passenger)
                .ticket(ticket)
                .boughtAt(boughtAt)
                .build();
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

    private record InMemoryTicketQueryRepository(List<Ticket> tickets) implements TicketQueryRepository {

        private InMemoryTicketQueryRepository(Ticket... tickets) {
            this(List.of(tickets));
        }

        @Override
        public List<Ticket> findAll() {
            return List.copyOf(tickets);
        }

        @Override
        public Optional<Ticket> findByTicketTypeAndDiscountTypeAndDurationMinutes(
                TicketType ticketType,
                DiscountType discountType,
                Integer durationMinutes
        ) {
            return tickets.stream()
                    .filter(ticket -> ticketType == ticket.getTicketType())
                    .filter(ticket -> discountType == ticket.getDiscountType())
                    .filter(ticket -> durationMinutes == null
                            ? ticket.getDurationMinutes() == null
                            : durationMinutes.equals(ticket.getDurationMinutes()))
                    .findFirst();
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

    private static class InMemoryPurchaseCommandRepository implements PurchaseCommandRepository {

        private final List<Purchase> saved = new ArrayList<>();

        @Override
        public Purchase save(Purchase purchase) {
            saved.add(purchase);
            if (purchase.getId() == null) {
                purchase.setId(java.util.UUID.randomUUID());
            }
            return purchase;
        }

        private Purchase singleSaved() {
            if (saved.isEmpty()) {
                return null;
            }
            return saved.getFirst();
        }
    }

    private record InMemoryPurchaseQueryRepository(List<Purchase> purchases) implements PurchaseQueryRepository {

        private InMemoryPurchaseQueryRepository(Purchase... purchases) {
            this(List.of(purchases));
        }

        private InMemoryPurchaseQueryRepository() {
            this(List.of());
        }

        @Override
        public Optional<Purchase> findById(java.util.UUID id) {
            return purchases.stream()
                    .filter(purchase -> id.equals(purchase.getId()))
                    .findFirst();
        }

        @Override
        public List<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(java.util.UUID passengerId) {
            return List.copyOf(purchases);
        }

        @Override
        public org.springframework.data.domain.Page<Purchase> findAllByPassengerIdOrderByBoughtAtDesc(
                java.util.UUID passengerId,
                org.springframework.data.domain.Pageable pageable
        ) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }
    }
}
