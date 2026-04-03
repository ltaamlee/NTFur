package ntfur.com.entity.dto.product;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {

    private Long id;

    private Long productId;

    @NotBlank(message = "URL hình ảnh không được để trống")
    private String imageUrl;

    private String publicId;

    @Size(max = 255, message = "Alt text không được vượt quá 255 ký tự")
    private String altText;

    private Integer displayOrder;

    private boolean isPrimary;

    private String createdAt;
}
