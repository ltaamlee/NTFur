package ntfur.com.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, columnDefinition = "nvarchar(50)")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "subtotal", precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_full_name", columnDefinition = "nvarchar(150)")
    private String shippingFullName;

    @Column(name = "shipping_phone", columnDefinition = "varchar(20)")
    private String shippingPhone;

    @Column(name = "shipping_address", columnDefinition = "nvarchar(MAX)")
    private String shippingAddress;

    @Column(name = "shipping_city", columnDefinition = "nvarchar(100)")
    private String shippingCity;

    @Column(name = "shipping_district", columnDefinition = "nvarchar(100)")
    private String shippingDistrict;

    @Column(name = "shipping_ward", columnDefinition = "nvarchar(100)")
    private String shippingWard;

    @Column(name = "shipping_notes", columnDefinition = "nvarchar(MAX)")
    private String shippingNotes;

    @Column(name = "coupon_code", columnDefinition = "nvarchar(50)")
    private String couponCode;

    @Column(name = "coupon_discount", precision = 18, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipping shipping;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "nvarchar(MAX)")
    private String cancellationReason;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED
    }

    public enum PaymentStatus {
        PENDING, PAID, PARTIALLY_PAID, REFUNDED, FAILED
    }

    public enum PaymentMethod {
        COD, BANK_TRANSFER, CREDIT_CARD, MOMO, ZALOPAY, VNPAY
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        orderDate = LocalDateTime.now();
        if (orderNumber == null || orderNumber.isEmpty()) {
            orderNumber = generateOrderNumber();
        }
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
        Order other = (Order) obj;
        return id != null && id.equals(other.id);
    }

    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("NTF-%s%s%s-%s%s%s",
                now.getYear() % 100,
                String.format("%02d", now.getMonthValue()),
                String.format("%02d", now.getDayOfMonth()),
                String.format("%02d", now.getHour()),
                String.format("%02d", now.getMinute()),
                String.format("%02d", now.getSecond())
        );
    }

    public void calculateTotal() {
        this.subtotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal
                .add(shippingFee != null ? shippingFee : BigDecimal.ZERO)
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .subtract(couponDiscount != null ? couponDiscount : BigDecimal.ZERO);
    }

    public String getShippingFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (shippingAddress != null) sb.append(shippingAddress);
        if (shippingWard != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(shippingWard);
        }
        if (shippingDistrict != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(shippingDistrict);
        }
        if (shippingCity != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(shippingCity);
        }
        return sb.toString();
    }

    public boolean isPaymentExpired() {
        if (paymentDeadline == null) return false;
        return LocalDateTime.now().isAfter(paymentDeadline);
    }
}
