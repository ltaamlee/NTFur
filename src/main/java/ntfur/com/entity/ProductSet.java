package ntfur.com.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "product_sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(name = "slug", unique = true, columnDefinition = "nvarchar(255)")
    private String slug;

    @Column(name = "description", columnDefinition = "nvarchar(MAX)")
    private String description;

    @Column(name = "image_url", columnDefinition = "nvarchar(MAX)")
    private String imageUrl;

    @Column(name = "total_price", precision = 18, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "productSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public void calculateTotalPrice() {
        this.totalPrice = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(name);
        }
        calculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
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
