package org.example.eticket.data.seed;

import org.example.eticket.data.entities.Ticket;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.TicketJpaRepository;
import org.example.eticket.data.repositories.UserJpaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InitialDataSeeder implements ApplicationRunner {

    private final UserJpaRepository userRepository;
    private final TicketJpaRepository ticketRepository;

    public InitialDataSeeder(UserJpaRepository userRepository, TicketJpaRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedTickets();
    }

    private void seedUsers() {
        List<UserSeed> seeds = List.of(
                new UserSeed(UserRole.PASSENGER, "passenger@example.com", "passenger", "Pat", "Passenger"),
                new UserSeed(UserRole.INSPECTOR, "inspector@example.com", "inspector", "Inez", "Inspector")
        );

        for (UserSeed seed : seeds) {
            if (!userRepository.existsByEmail(seed.email)) {
                userRepository.save(User.builder()
                        .role(seed.role)
                        .email(seed.email)
                        .passwordHash(seed.passwordHash)
                        .firstName(seed.firstName)
                        .lastName(seed.lastName)
                        .build());
            }
        }
    }

    private void seedTickets() {
        List<TicketSeed> seeds = List.of(
                new TicketSeed(TicketType.SINGLE_USE, DiscountType.NORMAL, new BigDecimal("3.40"), null),
                new TicketSeed(TicketType.SINGLE_USE, DiscountType.REDUCED, new BigDecimal("1.70"), null),
                new TicketSeed(TicketType.TIME_BASED, DiscountType.NORMAL, new BigDecimal("4.40"), 20),
                new TicketSeed(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("2.20"), 20),
                new TicketSeed(TicketType.TIME_BASED, DiscountType.NORMAL, new BigDecimal("6.00"), 60),
                new TicketSeed(TicketType.TIME_BASED, DiscountType.REDUCED, new BigDecimal("3.00"), 60),
                new TicketSeed(TicketType.PERIOD, DiscountType.NORMAL, new BigDecimal("110.00"), 43200),
                new TicketSeed(TicketType.PERIOD, DiscountType.REDUCED, new BigDecimal("55.00"), 43200)
        );

        for (TicketSeed seed : seeds) {
            if (!ticketRepository.existsByTicketTypeAndDiscountTypeAndDurationMinutes(
                    seed.ticketType,
                    seed.discountType,
                    seed.durationMinutes
            )) {
                ticketRepository.save(Ticket.builder()
                        .ticketType(seed.ticketType)
                        .discountType(seed.discountType)
                        .price(seed.price)
                        .durationMinutes(seed.durationMinutes)
                        .build());
            }
        }
    }

    private record UserSeed(UserRole role, String email, String passwordHash, String firstName, String lastName) {
    }

    private record TicketSeed(TicketType ticketType, DiscountType discountType, BigDecimal price,
                              Integer durationMinutes) {
    }
}


