package ntfur.com.entity.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String productSku;

    private String productImage;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private int quantity;

    private BigDecimal discountAmount;

    private BigDecimal taxAmount;

    private BigDecimal total;

    private Double weight;

    private String dimensions;

    private String color;

    private String variantInfo;

    private String notes;

    private String createdAt;
}
