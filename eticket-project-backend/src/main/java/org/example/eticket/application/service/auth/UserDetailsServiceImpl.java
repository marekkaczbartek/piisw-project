package org.example.eticket.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.repositories.user.UserQueryRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserQueryRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserData user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.withUsername(user.email())
                .password(user.passwordHash())
                .roles(user.role().name())
                .build();
    }
}

