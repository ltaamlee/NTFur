package ntfur.com.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/employee/orders")
@RequiredArgsConstructor
public class EmployeeOrderController {

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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> searchOrders(@RequestParam String keyword) {
        List<OrderDTO> orders = orderService.searchOrders(keyword);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(@PathVariable String status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
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
}
