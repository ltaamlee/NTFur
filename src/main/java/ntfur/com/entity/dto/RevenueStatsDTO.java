package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin thống kê doanh thu chi tiết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDTO {

    /** Loại thống kê: DAY, WEEK, MONTH, YEAR, CUSTOM */
    private String period;

    /** Ngày bắt đầu (cho CUSTOM) */
    private LocalDateTime startDate;

    /** Ngày kết thúc (cho CUSTOM) */
    private LocalDateTime endDate;

    /** Tổng doanh thu trong kỳ */
    private BigDecimal totalRevenue;

    /** Số đơn hàng trong kỳ */
    private long totalOrders;

    /** Doanh thu trung bình mỗi đơn */
    private BigDecimal averageOrderValue;

    /** Tổng số đơn đã thanh toán */
    private long paidOrders;

    /** Tổng số đơn chưa thanh toán */
    private long unpaidOrders;

    /** So sánh với kỳ trước (% tăng/giảm) */
    private Double growthRate;

    /** Dữ liệu biểu đồ theo từng mốc thời gian */
    private List<RevenueDataPoint> chartData;
}
