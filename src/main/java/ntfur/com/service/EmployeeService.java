package ntfur.com.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Employee;
import ntfur.com.entity.Employee.EmployeeStatus;
import ntfur.com.entity.dto.EmployeeDTO;
import ntfur.com.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id: " + id));
        return toDTO(employee);
    }

    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với email: " + email));
        return toDTO(employee);
    }

    public List<EmployeeDTO> searchEmployees(String keyword) {
        return employeeRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByStatus(String status) {
        return employeeRepository.findByStatus(EmployeeStatus.valueOf(status)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPasswordHash(passwordEncoder.encode(dto.getEmail().split("@")[0]));
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());
        
        if (dto.getHireDate() != null) {
            employee.setHireDate(LocalDateTime.parse(dto.getHireDate() + "T00:00:00"));
        }
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(LocalDateTime.parse(dto.getDateOfBirth() + "T00:00:00"));
        }
        
        if (dto.getGender() != null) {
            employee.setGender(Employee.Gender.valueOf(dto.getGender()));
        }
        employee.setAvatarUrl(dto.getAvatarUrl());
        employee.setStatus(dto.getStatus() != null ? EmployeeStatus.valueOf(dto.getStatus()) : EmployeeStatus.ACTIVE);

        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id: " + id));

        if (dto.getEmail() != null && !dto.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            employee.setEmail(dto.getEmail());
        }

        if (dto.getFullName() != null) employee.setFullName(dto.getFullName());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getAddress() != null) employee.setAddress(dto.getAddress());
        if (dto.getPosition() != null) employee.setPosition(dto.getPosition());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getSalary() != null) employee.setSalary(dto.getSalary());
        if (dto.getHireDate() != null) {
            employee.setHireDate(LocalDateTime.parse(dto.getHireDate() + "T00:00:00"));
        }
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(LocalDateTime.parse(dto.getDateOfBirth() + "T00:00:00"));
        }
        if (dto.getGender() != null) employee.setGender(Employee.Gender.valueOf(dto.getGender()));
        if (dto.getAvatarUrl() != null) employee.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getStatus() != null) employee.setStatus(EmployeeStatus.valueOf(dto.getStatus()));

        Employee updated = employeeRepository.save(employee);
        return toDTO(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy nhân viên với id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public long countEmployees() {
        return employeeRepository.count();
    }

    public long countByStatus(String status) {
        return employeeRepository.countByStatus(EmployeeStatus.valueOf(status));
    }

    private EmployeeDTO toDTO(Employee employee) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return EmployeeDTO.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .position(employee.getPosition())
                .department(employee.getDepartment())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate() != null ? employee.getHireDate().format(formatter) : null)
                .dateOfBirth(employee.getDateOfBirth() != null ? employee.getDateOfBirth().format(formatter) : null)
                .gender(employee.getGender() != null ? employee.getGender().name() : null)
                .avatarUrl(employee.getAvatarUrl())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .createdAt(employee.getCreatedAt() != null ? employee.getCreatedAt().toString() : null)
                .updatedAt(employee.getUpdatedAt() != null ? employee.getUpdatedAt().toString() : null)
                .build();
    }
}
