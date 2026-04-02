package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.DashboardDTO;
import ntfur.com.entity.dto.RevenueDataPoint;
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
}
