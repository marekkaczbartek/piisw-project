package org.example.eticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "bought_at", nullable = false)
    private LocalDateTime boughtAt;

    @Column(name = "punched_at")
    private LocalDateTime punchedAt;

    @Column(name = "punched_in", length = 50)
    private String punchedIn;

    /**
     * TIME_BASED: set at punch time → punchedAt + ticket.durationMinutes
     * PERIOD: set at purchase time → boughtAt + ticket.durationMinutes
     * SINGLE_USE: null
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

}
