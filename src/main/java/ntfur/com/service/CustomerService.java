package ntfur.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Customer;
import ntfur.com.entity.User;
import ntfur.com.entity.dto.customer.CustomerDTO;
import ntfur.com.entity.dto.customer.UpdateCustomerRequest;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
        return toDTO(customer);
    }

    public CustomerDTO getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với user id: " + userId));
        return toDTO(customer);
    }

    public List<CustomerDTO> searchCustomers(String keyword) {
        return customerRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> getTopCustomersBySpent() {
        return customerRepository.findTopCustomersBySpent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> getTopCustomersByOrders() {
        return customerRepository.findTopCustomersByOrders().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> getRecentCustomers(int limit) {
        return customerRepository.findAll().stream()
                .sorted((c1, c2) -> {
                    if (c1.getUser() == null || c1.getUser().getCreatedAt() == null) return 1;
                    if (c2.getUser() == null || c2.getUser().getCreatedAt() == null) return -1;
                    return c2.getUser().getCreatedAt().compareTo(c1.getUser().getCreatedAt());
                })
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));

        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            customer.getUser().setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            customer.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            customer.setWard(request.getWard());
        }
        if (request.getGender() != null) {
            customer.setGender(Customer.Gender.valueOf(request.getGender()));
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return toDTO(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khách hàng với id: " + id);
        }
        
        Customer customer = customerRepository.findById(id).orElseThrow();
        if (customer.getUser() != null) {
            userRepository.deleteById(customer.getUser().getId());
        } else {
            customerRepository.deleteById(id);
        }
    }

    public long countCustomers() {
        return customerRepository.countTotalCustomers();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = customerRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private CustomerDTO toDTO(Customer customer) {
        User user = customer.getUser();
        
        return CustomerDTO.builder()
                .id(customer.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getUser().getEmail())
                .address(customer.getAddress())
                .city(customer.getCity())
                .district(customer.getDistrict())
                .ward(customer.getWard())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .totalOrders(customer.getTotalOrders())
                .totalSpent(customer.getTotalSpent())
                .notes(customer.getNotes())
                .formattedAddress(customer.getFormattedAddress())
                .createdAt(user != null ? user.getCreatedAt() : null)
                .updatedAt(user != null ? user.getUpdatedAt() : null)
                .build();
    }
}
