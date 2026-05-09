package org.example.eticket.data.seed;

import org.example.eticket.data.repositories.TicketJpaRepository;
import org.example.eticket.data.repositories.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.seed.enabled=true")
class InitialDataSeederTest {

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Test
    void seedsUsersAndTickets() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(ticketRepository.count()).isGreaterThanOrEqualTo(8);
    }
}


