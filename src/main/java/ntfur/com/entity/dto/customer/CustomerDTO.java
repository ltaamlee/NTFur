package ntfur.com.entity.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    private Long userId;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 150, message = "Họ tên phải từ 2 đến 150 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String address;

    private String city;

    private String district;

    private String ward;

    private LocalDateTime dateOfBirth;

    private String gender;

    private String avatarUrl;

    private int totalOrders;

    private BigDecimal totalSpent;

    private String notes;

    private String formattedAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
