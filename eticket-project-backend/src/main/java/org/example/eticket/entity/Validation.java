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
@Table(name = "validations")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "checked_in", nullable = false, length = 50)
    private String checkedIn;

    @Column(nullable = false)
    private Boolean result;

}
