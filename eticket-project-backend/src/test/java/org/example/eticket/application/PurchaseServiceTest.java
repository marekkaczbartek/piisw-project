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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .durationMinutes(null)
                .build();
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
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ), passenger.getEmail());

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.PERIOD)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("12.00"))
                .durationMinutes(24 * 60)
                .build();
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
                ticket.getTicketType(),
                ticket.getDiscountType(),
                ticket.getDurationMinutes(),
                boughtAt
        ), passenger.getEmail());

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertNotNull(saved);
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), saved.getExpiresAt());
        assertEquals(boughtAt.plusMinutes(ticket.getDurationMinutes()), view.expiresAt());
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
                passenger.getEmail()
        ));

        // then
        assertEquals("Ticket not found", ex.getMessage());
    }

    @Test
    void punchesSingleUseTicket() {
        // given
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.SINGLE_USE)
                .discountType(DiscountType.NORMAL)
                .price(new BigDecimal("3.50"))
                .build();
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
                purchase.getId(),
                punchedAt,
                "BUS-10"
        ));

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.TIME_BASED)
                .discountType(DiscountType.REDUCED)
                .price(new BigDecimal("2.00"))
                .durationMinutes(30)
                .build();
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
                purchase.getId(),
                punchedAt,
                "TRAM-2"
        ));

        // then
        PurchaseData saved = purchaseRepository.singleSaved();
        assertEquals(punchedAt.plusMinutes(30), saved.getExpiresAt());
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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        PurchaseData purchase = purchase(UUID.randomUUID(), owner, ticket, LocalDateTime.now());
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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.SINGLE_USE)
                .build();
        UserData passenger = passenger("passenger@example.com");
        PurchaseData purchase = purchase(UUID.randomUUID(), passenger, ticket, LocalDateTime.now());
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
        TicketData ticket = TicketData.builder()
                .ticketType(TicketType.PERIOD)
                .durationMinutes(24 * 60)
                .build();
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
                () -> service.punchTicket(new PunchTicketCommand(purchase.getId(), LocalDateTime.now(), "BUS-10"))
        );

        // then
        assertEquals("Period tickets do not require punching", ex.getMessage());
    }

    private static UserData passenger(String email) {
        return UserData.builder()
                .id(java.util.UUID.randomUUID())
                .email(email)
                .role(UserRole.PASSENGER)
                .build();
    }


    private static PurchaseData purchase(UUID id, UserData passenger, TicketData ticket, LocalDateTime boughtAt) {
        return PurchaseData.builder()
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
                    .filter(ticket -> ticketType == ticket.getTicketType())
                    .filter(ticket -> discountType == ticket.getDiscountType())
                    .filter(ticket -> durationMinutes == null
                            ? ticket.getDurationMinutes() == null
                            : durationMinutes.equals(ticket.getDurationMinutes()))
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
                map.put(user.getEmail(), user);
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
            saved.add(purchase);
            if (purchase.getId() == null) {
                purchase.setId(java.util.UUID.randomUUID());
            }
            return purchase;
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
                    .filter(purchase -> id.equals(purchase.getId()))
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
