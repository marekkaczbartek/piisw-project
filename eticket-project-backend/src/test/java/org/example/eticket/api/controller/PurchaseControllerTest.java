package org.example.eticket.api.controller;

import org.example.eticket.application.exception.PeriodTicketPunchNotAllowedException;
import org.example.eticket.application.model.purchase.PunchTicketCommand;
import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.service.auth.JwtService;
import org.example.eticket.application.service.purchase.PurchaseService;
import org.example.eticket.config.JwtAuthenticationFilter;
import org.example.eticket.config.SecurityConfig;
import org.example.eticket.data.enums.DiscountType;
import org.example.eticket.data.enums.TicketType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseService purchaseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void punchTicketIsAccessibleWithoutAuthentication() throws Exception {
        UUID purchaseId = UUID.randomUUID();
        LocalDateTime boughtAt = LocalDateTime.of(2024, 5, 10, 8, 0);
        LocalDateTime punchedAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2024, 5, 10, 10, 0);
        PunchTicketView view = new PunchTicketView(
                purchaseId,
                TicketType.TIME_BASED,
                DiscountType.NORMAL,
                BigDecimal.valueOf(4.20),
                60,
                boughtAt,
                punchedAt,
                "BUS-10",
                expiresAt
        );

        when(purchaseService.punchTicket(any(PunchTicketCommand.class))).thenReturn(view);

        mockMvc.perform(patch("/purchases/{purchaseId}/punch", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punchedIn\":\"BUS-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchaseId.toString()))
                .andExpect(jsonPath("$.punchedIn").value("BUS-10"));

        ArgumentCaptor<PunchTicketCommand> captor = ArgumentCaptor.forClass(PunchTicketCommand.class);
        verify(purchaseService).punchTicket(captor.capture());
        assertEquals(purchaseId, captor.getValue().purchaseId());
        assertEquals("BUS-10", captor.getValue().punchedIn());
    }

    @Test
    void punchTicketReturnsBadRequestWhenServiceThrows() throws Exception {
        UUID purchaseId = UUID.randomUUID();
        when(purchaseService.punchTicket(any(PunchTicketCommand.class)))
                .thenThrow(new PeriodTicketPunchNotAllowedException());

        mockMvc.perform(patch("/purchases/{purchaseId}/punch", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punchedIn\":\"BUS-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Period tickets do not require punching"));
    }
}
