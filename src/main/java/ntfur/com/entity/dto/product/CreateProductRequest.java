package ntfur.com.entity.dto.product;

import java.util.List;

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
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2 đến 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    private Double price;

    private Double costPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    private Integer stock;

    @Size(max = 50, message = "SKU không được vượt quá 50 ký tự")
    private String sku;

    private String status;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private Long supplierId;

    private Double weight;

    private String dimensions;

    private String material;

    private String color;

    private Integer warrantyMonths;

    private boolean featured;

    private List<String> imageUrls;

    private List<ProductImageRequest> images;
}
