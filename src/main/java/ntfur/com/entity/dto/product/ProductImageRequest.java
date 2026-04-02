package ntfur.com.entity.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {

    private String imageUrl;

    private String publicId;

    private String altText;

    private int displayOrder;

    private boolean isPrimary;
}
