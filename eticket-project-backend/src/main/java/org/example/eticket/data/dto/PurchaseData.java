package org.example.eticket.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseData {

    private UUID id;
    private UserData passenger;
    private TicketData ticket;
    private LocalDateTime boughtAt;
    private LocalDateTime punchedAt;
    private String punchedIn;
    private LocalDateTime expiresAt;
}

