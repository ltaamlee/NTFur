package ntfur.com.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.repository.UserRepository;
import ntfur.com.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentInfo(
            Principal principal,
            @PathVariable Long orderId) {
        try {
            Map<String, Object> paymentInfo = paymentService.getPaymentInfo(orderId);
            return ResponseEntity.ok(ApiResponse.success(paymentInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPayment(
            Principal principal,
            @RequestBody ProcessPaymentRequest request) {
        try {
            Map<String, Object> result = paymentService.initiatePayment(
                    request.getOrderId(),
                    request.getPaymentMethod()
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            log.warn("Payment initiation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmPayment(
            Principal principal,
            @PathVariable Long orderId,
            @RequestParam(required = false) String paymentMethod) {
        try {
            Map<String, Object> result = paymentService.confirmPayment(orderId, paymentMethod);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            log.warn("Payment confirmation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(
            Principal principal,
            @PathVariable Long orderId) {
        try {
            Map<String, Object> paymentInfo = paymentService.getPaymentInfo(orderId);
            return ResponseEntity.ok(ApiResponse.success(paymentInfo));
        } catch (RuntimeException e) {
            log.warn("Get payment status error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelPayment(
            Principal principal,
            @PathVariable Long orderId) {
        try {
            Map<String, Object> result = paymentService.cancelPayment(orderId);
            return ResponseEntity.ok(ApiResponse.success("Hủy thanh toán thành công!", result));
        } catch (RuntimeException e) {
            log.warn("Payment cancellation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/vnpay/return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> vnpayReturn(
            @RequestParam Map<String, String> params) {
        try {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            
            if ("00".equals(vnp_ResponseCode)) {
                log.info("VNPay payment success for order: {}", vnp_TxnRef);
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                        "status", "PAID",
                        "message", "Thanh toán thành công qua VNPay"
                )));
            } else {
                log.warn("VNPay payment failed for order: {}, code: {}", vnp_TxnRef, vnp_ResponseCode);
                return ResponseEntity.badRequest().body(ApiResponse.error("Thanh toán không thành công. Vui lòng thử lại."));
            }
        } catch (Exception e) {
            log.error("VNPay return error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Xử lý kết quả thanh toán thất bại"));
        }
    }

    @lombok.Data
    public static class ProcessPaymentRequest {
        private Long orderId;
        private String paymentMethod;
    }
}
