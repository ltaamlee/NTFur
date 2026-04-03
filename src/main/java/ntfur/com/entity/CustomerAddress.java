package ntfur.com.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "address_name", columnDefinition = "nvarchar(100)")
    private String addressName;

    @Column(name = "recipient_name", columnDefinition = "nvarchar(150)")
    private String recipientName;

    @Column(name = "phone", columnDefinition = "nvarchar(20)")
    private String phone;

    @Column(name = "address_line", columnDefinition = "nvarchar(255)")
    private String addressLine;

    @Column(name = "city", columnDefinition = "nvarchar(100)")
    private String city;

    @Column(name = "district", columnDefinition = "nvarchar(100)")
    private String district;

    @Column(name = "ward", columnDefinition = "nvarchar(100)")
    private String ward;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType = AddressType.OTHER;

    @Column(name = "notes", columnDefinition = "nvarchar(MAX)")
    private String notes;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum AddressType {
        HOME, OFFICE, OTHER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine != null && !addressLine.isEmpty()) {
            sb.append(addressLine);
        }
        if (ward != null && !ward.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }
}
