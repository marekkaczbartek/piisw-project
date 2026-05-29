package org.example.eticket.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.EmailAlreadyRegisteredException;
import org.example.eticket.application.exception.UnauthorizedException;
import org.example.eticket.application.mapper.auth.AuthMapper;
import org.example.eticket.application.model.auth.AuthView;
import org.example.eticket.application.model.auth.LoginCommand;
import org.example.eticket.application.model.auth.RegisterCommand;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.enums.UserRole;
import org.example.eticket.data.repositories.user.UserJpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public AuthView register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = userRepository.save(User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(UserRole.PASSENGER)
                .build());

        return authMapper.toView(user, jwtService.generateToken(user));
    }

    public AuthView login(LoginCommand command) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return authMapper.toView(user, jwtService.generateToken(user));
    }
}
