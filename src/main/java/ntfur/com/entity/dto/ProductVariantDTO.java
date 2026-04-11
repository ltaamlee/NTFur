package ntfur.com.entity.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntfur.com.entity.ProductVariant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String variantName;
    private String attributeType;
    private String attributeValue;
    private String sku;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private int stock;
    private boolean active;
    private String imageUrl;

    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        if (variant == null) return null;
        
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .variantName(variant.getVariantName())
                .attributeType(variant.getAttributeType() != null ? variant.getAttributeType().name() : null)
                .attributeValue(variant.getAttributeValue())
                .sku(variant.getSku())
                .priceAdjustment(variant.getPriceAdjustment())
                .finalPrice(variant.getFinalPrice())
                .stock(variant.getStock())
                .active(variant.isActive())
                .imageUrl(variant.getImageUrl())
                .build();
    }
}
