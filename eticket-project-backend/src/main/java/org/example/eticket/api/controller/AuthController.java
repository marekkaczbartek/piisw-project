package org.example.eticket.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.auth.AuthResponse;
import org.example.eticket.api.dto.auth.LoginRequest;
import org.example.eticket.api.dto.auth.RegisterRequest;
import org.example.eticket.application.service.AuthService;
import org.example.eticket.application.model.auth.AuthView;
import org.example.eticket.application.model.auth.LoginCommand;
import org.example.eticket.application.model.auth.RegisterCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static AuthResponse toResponse(AuthView view) {
        return new AuthResponse(
                view.token(),
                view.id(),
                view.email(),
                view.role(),
                view.firstName(),
                view.lastName()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthView view = authService.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(toResponse(view));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthView view = authService.register(new RegisterCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(view));
    }
}
