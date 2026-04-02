package ntfur.com.entity.dto.product;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {

    private Long id;

    private String name;

    private String sku;

    private String mainImage;

    private BigDecimal price;

    private long totalSold;

    private BigDecimal totalRevenue;
}
