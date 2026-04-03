package ntfur.com.entity.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressDTO {

    private Long id;

    private Long customerId;

    private String addressName;

    private String recipientName;

    private String phone;

    private String addressLine;

    private String city;

    private String district;

    private String ward;

    private String fullAddress;

    private boolean isDefault;

    private String addressType;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
