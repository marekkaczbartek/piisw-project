package org.example.eticket.application;

import org.example.eticket.application.model.purchase.MakePurchaseCommand;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.model.purchase.PunchTicketCommand;
import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.service.PurchaseService;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.PurchaseCommandRepository;
import org.example.eticket.data.repositories.PurchaseQueryRepository;
import org.example.eticket.data.repositories.TicketQueryRepository;
import org.example.eticket.data.repositories.UserQueryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsSingleUsePurchaseWithoutExpiry() {
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .durationMinutes(null)
                .build();
        User passenger = passenger("passenger@example.com");
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(List.of())
        );
        setPassenger(passenger.getEmail());
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);

        PurchaseView view = service.makePurchase(new MakePurchaseCommand(
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ));

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
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("12.00"))
                .durationMinutes(24 * 60)
                .build();
        User passenger = passenger("passenger@example.com");
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(List.of())
        );
        setPassenger(passenger.getEmail());
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);

        PurchaseView view = service.makePurchase(new MakePurchaseCommand(
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ));

        Purchase saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), saved.getExpiresAt());
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), view.expiresAt());
    }

    @Test
    void throwsWhenTicketDoesNotExist() {
        User passenger = passenger("passenger@example.com");
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(List.of())
        );
        setPassenger(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.makePurchase(
                new MakePurchaseCommand(TicketType.SINGLE_USE, DiscountType.NORMAL, null, LocalDateTime.now())
        ));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void throwsWhenPassengerNotAuthenticated() {
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryUserQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(List.of())
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.makePurchase(
                new MakePurchaseCommand(TicketType.SINGLE_USE, DiscountType.NORMAL, null, LocalDateTime.now())
        ));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void throwsWhenUserIsNotPassenger() {
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .build();
        User inspector = User.builder()
                .id(java.util.UUID.randomUUID())
                .email("inspector@example.com")
                .role(UserRole.INSPECTOR)
                .build();
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(List.of())
        );
        setPassenger(inspector.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.makePurchase(
                new MakePurchaseCommand(TicketType.SINGLE_USE, DiscountType.NORMAL, null, LocalDateTime.now())
        ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void punchesSingleUseTicket() {
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(passenger.getEmail());
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);

        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.getId(),
                punchedAt,
                "BUS-10"
        ));

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
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.TIME_BASED)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("2.00"))
                .durationMinutes(30)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.of(2024, 5, 10, 8, 0));
        InMemoryPurchaseCommandRepository purchaseRepository = new InMemoryPurchaseCommandRepository();
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                purchaseRepository,
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(passenger.getEmail());
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);

        PunchTicketView view = service.punchTicket(new PunchTicketCommand(
                purchase.getId(),
                punchedAt,
                "TRAM-2"
        ));

        Purchase saved = purchaseRepository.singleSaved();
        assertEquals(punchedAt.plusMinutes(30), saved.getExpiresAt());
        assertEquals(punchedAt.plusMinutes(30), view.expiresAt());
    }

    @Test
    void throwsWhenPunchPurchaseDoesNotExist() {
        User passenger = passenger("passenger@example.com");
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository()
        );
        setPassenger(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void throwsWhenPunchPassengerNotAuthenticated() {
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(),
                new InMemoryUserQueryRepository(),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(UUID.randomUUID(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void throwsWhenPunchUserIsNotPassenger() {
        User inspector = User.builder()
                .id(UUID.randomUUID())
                .email("inspector@example.com")
                .role(UserRole.INSPECTOR)
                .build();
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(UUID.randomUUID(), inspector, ticket, LocalDateTime.now());
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(inspector),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(inspector.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(purchase.getId(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void throwsWhenPunchPassengerIsNotOwner() {
        User passenger = passenger("passenger@example.com");
        User owner = passenger("owner@example.com");
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        Purchase purchase = purchase(UUID.randomUUID(), owner, ticket, LocalDateTime.now());
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(purchase.getId(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void throwsWhenPunchTicketAlreadyPunched() {
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        purchase.setPunchedAt(LocalDateTime.of(2024, 5, 10, 9, 0));
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(purchase.getId(), LocalDateTime.of(2024, 5, 10, 10, 0), "BUS-10")
        ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void throwsWhenPunchPeriodTicket() {
        Ticket ticket = Ticket.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
        User passenger = passenger("passenger@example.com");
        Purchase purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
        PurchaseService service = new PurchaseService(
                new InMemoryTicketQueryRepository(ticket),
                new InMemoryUserQueryRepository(passenger),
                new InMemoryPurchaseCommandRepository(),
                new InMemoryPurchaseQueryRepository(purchase)
        );
        setPassenger(passenger.getEmail());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.punchTicket(
                new PunchTicketCommand(purchase.getId(), LocalDateTime.now(), "BUS-10")
        ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    private static User passenger(String email) {
        return User.builder()
                .id(java.util.UUID.randomUUID())
                .email(email)
                .role(UserRole.PASSENGER)
                .build();
    }

    private static void setPassenger(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of())
        );
    }

    private static Purchase purchase(UUID id, User passenger, Ticket ticket, LocalDateTime boughtAt) {
        return Purchase.builder()
                .id(id)
                .passenger(passenger)
                .ticket(ticket)
                .boughtAt(boughtAt)
                .build();
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
            return saved.get(0);
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
