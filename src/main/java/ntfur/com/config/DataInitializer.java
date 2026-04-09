package ntfur.com.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Customer;
import ntfur.com.entity.User;
import ntfur.com.entity.User.UserRole;
import ntfur.com.entity.User.UserStatus;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.UserRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Tài khoản Admin - luôn ACTIVE (không cần xác thực email)
        createAdminUser(
            "admin@ntfurniture.com",
            "admin123",
            "Admin Quản Trị",
            UserRole.ADMIN,
            "0909123456"
        );
        
        // Tài khoản Staff - luôn ACTIVE
        createAdminUser(
            "staff@ntfurniture.com",
            "staff123",
            "Nhân Viên Kho",
            UserRole.STAFF,
            "0909876543"
        );
        
        // Tài khoản Manager - luôn ACTIVE
        createAdminUser(
            "manager@ntfurniture.com",
            "manager123",
            "Quản lý",
            UserRole.MANAGER,
            "0909123457"
        );
        
        // Tài khoản Customer - mặc định PENDING (cần xác thực email)
        createCustomerUser(
            "customer@test.com",
            "customer123",
            "Khách Hàng Test",
            "0909123458"
        );
        
        log.info("=== Tài khoản hệ thống ===");
        log.info("Admin: admin@ntfurniture.com / admin123");
        log.info("Staff: staff@ntfurniture.com / staff123");
        log.info("Manager: manager@ntfurniture.com / manager123");
        log.info("Customer: customer@test.com / customer123 (cần xác thực email)");
        log.info("========================");
    }
    
    /**
     * Tạo tài khoản Admin/Staff/Manager - luôn ACTIVE
     */
    private void createAdminUser(String email, String password, String fullName, UserRole role, String phone) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE); // Luôn active cho admin/staff/manager
            user.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            userRepository.save(user);
            
            log.info("Đã tạo tài khoản {}: {} ({})", role, email, password);
        } else {
            log.info("Tài khoản {} đã tồn tại: {}", role, email);
        }
    }
    
    /**
     * Tạo tài khoản Customer - mặc định PENDING (cần xác thực email)
     */
    private void createCustomerUser(String email, String password, String fullName, String phone) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.PENDING); // Cần xác thực email
            user.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            user = userRepository.save(user);
            
            // Tạo Customer profile
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setAddress("Địa chỉ mặc định");
            customer.setCity("TP Hồ Chí Minh");
            customerRepository.save(customer);
            
            log.info("Đã tạo tài khoản Customer: {} ({})", email, password);
        } else {
            log.info("Tài khoản Customer đã tồn tại: {}", email);
        }
    }
}
