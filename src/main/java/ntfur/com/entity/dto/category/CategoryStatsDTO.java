package ntfur.com.entity.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDTO {

    private Long id;

    private String name;

    private String slug;

    private int productCount;

    private long totalSold;

    private double percentage;
}
