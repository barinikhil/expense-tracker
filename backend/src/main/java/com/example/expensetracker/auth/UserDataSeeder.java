package com.example.expensetracker.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUserIfMissing("u001", "pass111");
        seedUserIfMissing("u002", "pass111");
    }

    private void seedUserIfMissing(String username, String rawPassword) {
        userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setUsername(username);
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    user.setActive(true);
                    return userRepository.save(user);
                });
    }
}
