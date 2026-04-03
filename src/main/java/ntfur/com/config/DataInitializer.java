package ntfur.com.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Customer;
import ntfur.com.entity.User;
import ntfur.com.entity.User.UserRole;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserWithCustomer(
            "admin@ntfurniture.com",
            "admin123",
            "Admin Quản Trị",
            UserRole.ADMIN
        );
        
        createUserWithCustomer(
            "staff@ntfurniture.com",
            "staff123",
            "Nhân Viên Kho",
            UserRole.STAFF
        );
        
        createUserWithCustomer(
            "customer@test.com",
            "customer123",
            "Khách Hàng Test",
            UserRole.CUSTOMER
        );
        
        log.info("Test accounts initialization completed");
    }
    
    private void createUserWithCustomer(String email, String password, String fullName, UserRole role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
            
            // Tạo Customer cho User (trừ Admin và Staff)
            if (role == UserRole.CUSTOMER) {
                Customer customer = new Customer();
                customer.setUser(user);
                customer.setFullName(fullName);
                customer.setPhone("0909123456");
                customer.setAddress("123 Đường Test");
                customer.setCity("TP Hồ Chí Minh");
                customer.setDistrict("Quận 1");
                customer.setWard("Phường 1");
                customerRepository.save(customer);
                log.info("Created customer profile for: {}", email);
            }
            
            log.info("Created test account: {} ({}) - Password: {}", email, role, password);
        } else {
            log.info("Test account already exists: {} ({})", email, role);
        }
    }
}
