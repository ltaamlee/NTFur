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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "variant_name", columnDefinition = "nvarchar(255)")
    private String variantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type")
    private AttributeType attributeType;

    @Column(name = "attribute_value", columnDefinition = "nvarchar(100)")
    private String attributeValue;

    @Column(name = "sku", columnDefinition = "nvarchar(50)")
    private String sku;

    @Column(name = "price_adjustment", precision = 18, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "image_url", columnDefinition = "nvarchar(MAX)")
    private String imageUrl;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum AttributeType {
        SIZE,       // Kích thước
        COLOR,      // Màu sắc
        MATERIAL,   // Chất liệu
        STYLE,      // Phong cách
        OTHER       // Khác
    }

    public BigDecimal getFinalPrice() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().add(priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO);
        }
        return priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        active = true;
        if (sku == null || sku.isEmpty()) {
            sku = generateVariantSku();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateVariantSku() {
        String baseSku = product != null && product.getSku() != null ? product.getSku() : "VAR";
        String attrType = attributeType != null ? attributeType.name().substring(0, 3) : "OTH";
        String attrVal = attributeValue != null ? attributeValue.substring(0, Math.min(3, attributeValue.length())).toUpperCase() : "VAL";
        return baseSku + "-" + attrType + "-" + attrVal;
    }
}
