package ntfur.com.entity.dto.category;

import java.time.LocalDateTime;
import java.util.List;

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
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
    private String slug;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    private String imageUrl;

    private String status;

    private int displayOrder;
    
    // Parent category info
    private Long parentId;
    private String parentName;
    
    // Child categories
    private List<CategoryDTO> children;
    
    // Product count
    private int productCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
