package ntfur.com.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shippings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "tracking_number", columnDefinition = "nvarchar(100)")
    private String trackingNumber;

    @Column(name = "carrier", columnDefinition = "nvarchar(100)")
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method")
    private ShippingMethod shippingMethod;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShippingStatus status = ShippingStatus.PENDING;

    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;

    @Column(name = "pickup_time", columnDefinition = "nvarchar(50)")
    private String pickupTime;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "delivery_time", columnDefinition = "nvarchar(50)")
    private String deliveryTime;

    @Column(name = "receiver_name", columnDefinition = "nvarchar(150)")
    private String receiverName;

    @Column(name = "receiver_phone", columnDefinition = "varchar(20)")
    private String receiverPhone;

    @Column(name = "delivery_address", columnDefinition = "nvarchar(MAX)")
    private String deliveryAddress;

    @Column(name = "delivery_notes", columnDefinition = "nvarchar(MAX)")
    private String deliveryNotes;

    @Column(name = "installation_required")
    private boolean installationRequired = false;

    @Column(name = "installation_fee", precision = 18, scale = 2)
    private BigDecimal installationFee = BigDecimal.ZERO;

    @Column(name = "installation_completed_at")
    private LocalDateTime installationCompletedAt;

    @Column(name = "installation_notes", columnDefinition = "nvarchar(MAX)")
    private String installationNotes;

    @Column(name = "attempt_count")
    private int attemptCount = 0;

    @Column(name = "last_attempt_date")
    private LocalDateTime lastAttemptDate;

    @Column(name = "failure_reason", columnDefinition = "nvarchar(MAX)")
    private String failureReason;

    @Column(name = "signed_by", columnDefinition = "nvarchar(150)")
    private String signedBy;

    @Column(name = "signature_url", columnDefinition = "nvarchar(MAX)")
    private String signatureUrl;

    @Column(name = "photo_url", columnDefinition = "nvarchar(MAX)")
    private String photoUrl;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum ShippingMethod {
        STANDARD, EXPRESS, SAME_DAY, SCHEDULED, PICKUP
    }

    public enum ShippingStatus {
        PENDING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED, CANCELLED
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

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Shipping other = (Shipping) obj;
        return id != null && id.equals(other.id);
    }

    public BigDecimal getTotalFee() {
        return shippingFee.add(installationRequired && installationFee != null ? installationFee : BigDecimal.ZERO);
    }
}
