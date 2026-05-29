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
public class ValidationData {

    private UUID id;
    private UserData inspector;
    private PurchaseData purchase;
    private LocalDateTime checkedAt;
    private String checkedIn;
    private Boolean result;
}

