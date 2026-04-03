package ntfur.com.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "nvarchar(100)")
    private String name;

    @Column(name = "slug", unique = true, columnDefinition = "nvarchar(100)")
    private String slug;

    @Column(name = "code", unique = true, columnDefinition = "nvarchar(50)")
    private String code;

    @Column(name = "description", columnDefinition = "nvarchar(MAX)")
    private String description;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", columnDefinition = "nvarchar(20)")
    private String phone;

    @Column(name = "address", columnDefinition = "nvarchar(255)")
    private String address;

    @Column(name = "city", columnDefinition = "nvarchar(50)")
    private String city;

    @Column(name = "country", columnDefinition = "nvarchar(50)")
    private String country = "Vietnam";

    @Column(name = "tax_code", columnDefinition = "nvarchar(50)")
    private String taxCode;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name", columnDefinition = "nvarchar(100)")
    private String bankName;

    @Column(name = "contact_person", columnDefinition = "nvarchar(100)")
    private String contactPerson;

    @Column(name = "contact_phone", columnDefinition = "nvarchar(20)")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "website")
    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @Column(name = "notes", columnDefinition = "nvarchar(MAX)")
    private String notes;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum SupplierStatus {
        ACTIVE, INACTIVE, PENDING, BLOCKED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(name);
        }
        if (code == null || code.isEmpty()) {
            code = "SUP-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateSlug(String name) {
        if (name == null) return "";
        String slug = name.toLowerCase().trim();
        slug = slug.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        slug = slug.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        slug = slug.replaceAll("[ìíịỉĩ]", "i");
        slug = slug.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        slug = slug.replaceAll("[ùúụủũưừứựửữ]", "u");
        slug = slug.replaceAll("[ỳýỵỷỹ]", "y");
        slug = slug.replaceAll("đ", "d");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }
}
