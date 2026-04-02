package ntfur.com.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    private Long id;
    private String name;
    private String slug;
    private String code;
    private String description;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String taxCode;
    private String bankAccount;
    private String bankName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String website;
    private String logoUrl;
    private Double rating;
    private Integer totalOrders;
    private Double totalAmount;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
