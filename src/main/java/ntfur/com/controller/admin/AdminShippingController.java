package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.ShippingDTO;
import ntfur.com.entity.dto.UpdateShippingRequest;
import ntfur.com.service.ShippingService;

@RestController
@RequestMapping("/api/admin/shippings")
@RequiredArgsConstructor
public class AdminShippingController {

    private final ShippingService shippingService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShippingDTO>>> getAllShippings() {
        List<ShippingDTO> shippings = shippingService.getAllShippings();
        return ResponseEntity.ok(ApiResponse.success(shippings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingDTO>> getShippingById(@PathVariable Long id) {
        try {
            ShippingDTO shipping = shippingService.getShippingById(id);
            return ResponseEntity.ok(ApiResponse.success(shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ShippingDTO>> getShippingByOrderId(@PathVariable Long orderId) {
        try {
            ShippingDTO shipping = shippingService.getShippingByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success(shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<ShippingDTO>> getShippingByTrackingNumber(@PathVariable String trackingNumber) {
        try {
            ShippingDTO shipping = shippingService.getShippingByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(ApiResponse.success(shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ShippingDTO>>> getShippingsByStatus(@PathVariable String status) {
        try {
            List<ShippingDTO> shippings = shippingService.getShippingsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(shippings));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pending-installations")
    public ResponseEntity<ApiResponse<List<ShippingDTO>>> getPendingInstallations() {
        List<ShippingDTO> shippings = shippingService.getPendingInstallations();
        return ResponseEntity.ok(ApiResponse.success(shippings));
    }

    @GetMapping("/delivered-pending-installations")
    public ResponseEntity<ApiResponse<List<ShippingDTO>>> getDeliveredPendingInstallations() {
        List<ShippingDTO> shippings = shippingService.getDeliveredPendingInstallations();
        return ResponseEntity.ok(ApiResponse.success(shippings));
    }

    @GetMapping("/failed-deliveries")
    public ResponseEntity<ApiResponse<List<ShippingDTO>>> getFailedDeliveries() {
        List<ShippingDTO> shippings = shippingService.getFailedDeliveries();
        return ResponseEntity.ok(ApiResponse.success(shippings));
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ShippingDTO>> createShipping(@PathVariable Long orderId, @RequestBody ShippingDTO request) {
        try {
            ShippingDTO shipping = shippingService.createShipping(orderId, request);
            return ResponseEntity.ok(ApiResponse.success("Tạo vận chuyển thành công", shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingDTO>> updateShipping(@PathVariable Long id, @RequestBody UpdateShippingRequest request) {
        try {
            ShippingDTO shipping = shippingService.updateShipping(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật vận chuyển thành công", shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShippingDTO>> updateShippingStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            ShippingDTO shipping = shippingService.updateShippingStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái vận chuyển thành công", shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/installation-complete")
    public ResponseEntity<ApiResponse<ShippingDTO>> markInstallationComplete(@PathVariable Long id, @RequestParam(required = false) String notes) {
        try {
            ShippingDTO shipping = shippingService.markInstallationComplete(id, notes);
            return ResponseEntity.ok(ApiResponse.success("Đánh dấu lắp đặt hoàn thành", shipping));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShipping(@PathVariable Long id) {
        try {
            shippingService.deleteShipping(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa vận chuyển thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status-count/{status}")
    public ResponseEntity<ApiResponse<Long>> countShippingsByStatus(@PathVariable String status) {
        try {
            long count = shippingService.countShippingsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
