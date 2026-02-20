package com.example.expensetracker.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class UserContext {

    private final UserRepository userRepository;

    public UserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String currentUsername() {
        return extractAuthentication().map(Authentication::getName)
                .filter(name -> !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    public Long currentUserId() {
        String username = currentUsername();
        return userRepository.findByUsernameIgnoreCase(username)
                .map(AppUser::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Optional<Authentication> extractAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}
