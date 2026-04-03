package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.service.OrderService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            OrderDTO order = orderService.getOrderByNumber(orderNumber);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> searchOrders(@RequestParam String keyword) {
        List<OrderDTO> orders = orderService.searchOrders(keyword);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getRecentOrders(@RequestParam(defaultValue = "5") int limit) {
        List<OrderDTO> orders = orderService.getRecentOrders(limit);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderDTO> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            OrderDTO order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<OrderDTO>> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        try {
            OrderDTO order = orderService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thanh toán thành công", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa đơn hàng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countOrders() {
        long count = orderService.countOrders();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/status-counts")
    public ResponseEntity<ApiResponse<List<Object[]>>> getOrderStatusCounts() {
        List<Object[]> counts = orderService.getOrderStatusCounts();
        return ResponseEntity.ok(ApiResponse.success(counts));
    }
}
