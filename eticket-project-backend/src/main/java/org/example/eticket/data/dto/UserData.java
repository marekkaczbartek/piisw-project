package org.example.eticket.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eticket.data.enums.UserRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserData {

    private UUID id;
    private UserRole role;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
}

