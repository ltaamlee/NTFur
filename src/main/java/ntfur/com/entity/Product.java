package ntfur.com.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(name = "description", columnDefinition = "nvarchar(MAX)")
    private String description;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 18, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @Column(name = "sku", unique = true, columnDefinition = "nvarchar(50)")
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnore
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_set_id")
    @JsonIgnore
    private ProductSet productSet;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplierProduct> supplierProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dimensions", columnDefinition = "nvarchar(100)")
    private String dimensions;

    @Column(name = "material", columnDefinition = "nvarchar(255)")
    private String material;

    @Column(name = "color", columnDefinition = "nvarchar(50)")
    private String color;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sku == null || sku.isEmpty()) {
            sku = generateSkuFromName(name);
        }
    }

    private String generateSkuFromName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "SKU-" + String.format("%03d", new java.util.Random().nextInt(1000));
        }

        // Lấy chữ cái đầu của mỗi từ, viết hoa
        StringBuilder initials = new StringBuilder();
        String[] words = productName.trim().split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        // Thêm số ngẫu nhiên 3 chữ số
        int randomNum = new java.util.Random().nextInt(1000);
        return initials.toString() + "-" + String.format("%03d", randomNum);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getMainImage() {
        return images.stream()
                .filter(img -> img.isPrimary())
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
    }
    
    
}
