package ntfur.com.entity.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntfur.com.entity.dto.product.ProductDTO;

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
    private List<ProductDTO> products;
    private Integer productCount;
    private String createdAt;
    private String updatedAt;
    
    // Fields for creating/updating products
    private List<String> productNames;  // Tên sản phẩm mới cần tạo
    private List<Long> productIds;        // ID sản phẩm có sẵn cần gán
}
