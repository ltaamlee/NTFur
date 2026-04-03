package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntfur.com.entity.dto.product.ProductDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSetDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private BigDecimal totalPrice;
    private boolean active;
    private List<ProductDTO> products;
    private Integer productCount;
    private String createdAt;
    private String updatedAt;
}
