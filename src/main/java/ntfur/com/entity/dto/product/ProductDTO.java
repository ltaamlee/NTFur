package ntfur.com.entity.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2 đến 255 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Giá gốc không được nhỏ hơn 0")
    private BigDecimal costPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private Integer stock;

    @Size(max = 50, message = "SKU không được vượt quá 50 ký tự")
    private String sku;

    private String status;

    private Long categoryId;

    private String categoryName;

    private Long supplierId;

    private String supplierName;

    @Min(value = 0, message = "Trọng lượng không được nhỏ hơn 0")
    private Double weight;

    @Size(max = 100, message = "Kích thước không được vượt quá 100 ký tự")
    private String dimensions;

    @Size(max = 255, message = "Chất liệu không được vượt quá 255 ký tự")
    private String material;

    @Size(max = 50, message = "Màu sắc không được vượt quá 50 ký tự")
    private String color;

    @Min(value = 0, message = "Thời hạn bảo hành không được nhỏ hơn 0")
    private Integer warrantyMonths;

    private boolean featured;

    private String mainImage;

    private List<ProductImageDTO> images;

    private int viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
