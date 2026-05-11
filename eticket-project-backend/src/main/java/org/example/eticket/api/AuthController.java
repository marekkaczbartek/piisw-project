package org.example.eticket.api;

import lombok.RequiredArgsConstructor;
import org.example.eticket.api.dto.AuthResponse;
import org.example.eticket.api.dto.LoginRequest;
import org.example.eticket.api.dto.RegisterRequest;
import org.example.eticket.service.AuthService;
import org.example.eticket.service.model.AuthView;
import org.example.eticket.service.model.LoginCommand;
import org.example.eticket.service.model.RegisterCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthView view = authService.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(toResponse(view));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthView view = authService.register(new RegisterCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(view));
    }

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
}
