package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntfur.com.entity.dto.category.CategoryStatsDTO;
import ntfur.com.entity.dto.customer.CustomerDTO;
import ntfur.com.entity.dto.product.TopProductDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long totalProducts;

    private long totalCustomers;

    private long totalOrders;

    private long pendingOrders;

    private long confirmedOrders;

    private long processingOrders;

    private long shippedOrders;

    private long deliveredOrders;

    private long returnedOrders;

    private long cancelledOrders;

    private BigDecimal totalRevenue;

    private BigDecimal todayRevenue;

    private BigDecimal monthRevenue;

    private BigDecimal yearRevenue;

    private List<RevenueDataPoint> revenueChart;

    private List<TopProductDTO> topProducts;

    private List<OrderDTO> recentOrders;

    private List<CustomerDTO> recentCustomers;

    private List<CategoryStatsDTO> categoryStats;
}
