package ntfur.com.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.DashboardDTO;
import ntfur.com.entity.dto.RevenueDataPoint;
import ntfur.com.entity.dto.RevenueStatsDTO;
import ntfur.com.service.DashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboardData() {
        DashboardDTO dashboard = dashboardService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<List<RevenueDataPoint>>> getRevenueChart(@RequestParam(defaultValue = "30") int days) {
        List<RevenueDataPoint> chartData = dashboardService.getRevenueChart(days);
        return ResponseEntity.ok(ApiResponse.success(chartData));
    }

    /**
     * API thống kê doanh thu theo kỳ (ngày, tuần, tháng, năm hoặc tùy chỉnh)
     * @param period Loại kỳ: DAY, WEEK, MONTH, YEAR, CUSTOM
     * @param startDate Ngày bắt đầu (cho CUSTOM, format: yyyy-MM-dd)
     * @param endDate Ngày kết thúc (cho CUSTOM, format: yyyy-MM-dd)
     */
    @GetMapping("/revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsDTO>> getRevenueStats(
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            RevenueStatsDTO stats = dashboardService.getRevenueStats(period, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
