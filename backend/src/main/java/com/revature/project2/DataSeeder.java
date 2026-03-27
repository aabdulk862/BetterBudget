package com.revature.project2;

import com.revature.project2.models.User;
import com.revature.project2.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedUser("admin", "password", "admin@gooderbudget.com",
                    "Admin", "User", "ROLE_MANAGER");
            seedUser("user", "password", "user@gooderbudget.com",
                    "Default", "User", "ROLE_EMPLOYEE");
        } catch (Exception e) {
            logger.warn("Could not seed default users: {}", e.getMessage());
        }
    }

    private void seedUser(String username, String rawPassword, String email,
                          String firstName, String lastName, String role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            userRepository.save(user);
            logger.info("Default user created — username: {}, role: {}", username, role);
        }
    }
}
