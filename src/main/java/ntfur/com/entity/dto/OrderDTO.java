package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private String orderNumber;

    private Long userId;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private String status;

    private String paymentStatus;

    private String paymentMethod;

    private BigDecimal subtotal;

    private BigDecimal shippingFee;

    private BigDecimal discountAmount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String shippingFullName;

    private String shippingPhone;

    private String shippingAddress;

    private String shippingCity;

    private String shippingDistrict;

    private String shippingWard;

    private String shippingNotes;

    private String shippingFullAddress;

    private String couponCode;

    private BigDecimal couponDiscount;

    private List<OrderItemDTO> items;

    private ShippingDTO shipping;

    private LocalDateTime orderDate;

    private LocalDateTime confirmedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    private Long payosOrderCode;

    private String payosCheckoutUrl;

    private LocalDateTime paymentDeadline;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
