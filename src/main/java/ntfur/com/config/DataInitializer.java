package ntfur.com.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.User;
import ntfur.com.entity.User.UserRole;
import ntfur.com.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createTestUserIfNotExists(
            "admin@ntfurniture.com",
            "admin123",
            "Admin Quản Trị",
            UserRole.ADMIN
        );
        
        createTestUserIfNotExists(
            "staff@ntfurniture.com",
            "staff123",
            "Nhân Viên Kho",
            UserRole.STAFF
        );
        
        createTestUserIfNotExists(
            "customer@test.com",
            "customer123",
            "Khách Hàng Test",
            UserRole.CUSTOMER
        );
        
        log.info("Test accounts initialization completed");
    }
    
    private void createTestUserIfNotExists(String email, String password, String fullName, UserRole role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
            log.info("Created test account: {} ({}) - Password: {}", email, role, password);
        } else {
            log.info("Test account already exists: {} ({})", email, role);
        }
    }
}
