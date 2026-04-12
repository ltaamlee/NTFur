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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouse_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_code", unique = true, nullable = false, columnDefinition = "nvarchar(50)")
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", columnDefinition = "nvarchar(255)")
    private String productName;

    @Column(name = "product_sku", columnDefinition = "nvarchar(50)")
    private String productSku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "notes", columnDefinition = "nvarchar(MAX)")
    private String notes;

    @Column(name = "performed_by", columnDefinition = "nvarchar(100)")
    private String performedBy;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        IMPORT, EXPORT, ADJUSTMENT, RETURN
    }

    public enum ReferenceType {
        IMPORT_RECEIPT, ORDER, RETURN_ORDER, MANUAL
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, CANCELLED, FAILED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionCode == null || transactionCode.isEmpty()) {
            String prefix = transactionType != null ? transactionType.name().substring(0, 3).toUpperCase() : "TRX";
            transactionCode = prefix + "-" + System.currentTimeMillis();
        }
    }
}