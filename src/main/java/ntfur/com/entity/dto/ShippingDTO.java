package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDTO {

    private Long id;

    private Long orderId;

    private String orderNumber;

    private String trackingNumber;

    private String carrier;

    private String shippingMethod;

    private BigDecimal shippingFee;

    private Integer estimatedDays;

    private String status;

    private LocalDateTime pickupDate;

    private String pickupTime;

    private LocalDateTime deliveryDate;

    private String deliveryTime;

    private String receiverName;

    private String receiverPhone;

    private String deliveryAddress;

    private String deliveryNotes;

    private boolean installationRequired;

    private BigDecimal installationFee;

    private LocalDateTime installationCompletedAt;

    private String installationNotes;

    private int attemptCount;

    private LocalDateTime lastAttemptDate;

    private String failureReason;

    private String signedBy;

    private String signatureUrl;

    private String photoUrl;

    private BigDecimal totalFee;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
