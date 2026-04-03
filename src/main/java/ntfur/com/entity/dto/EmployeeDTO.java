package ntfur.com.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String position;
    private String department;
    private Double salary;
    private String hireDate;
    private String dateOfBirth;
    private String gender;
    private String avatarUrl;
    private String status;
    private String createdAt;
    private String updatedAt;
}
