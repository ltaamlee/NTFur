package ntfur.com.controller.paymentgateway;

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
import ntfur.com.repository.OrderRepository;
import ntfur.com.service.PayOSService;
import ntfur.com.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PayOSService payOSService;
    private final OrderRepository orderRepository;

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

    @PostMapping("/cod")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processCodPayment(
            Principal principal,
            @RequestBody ProcessPaymentRequest request) {
        try {
            Map<String, Object> result = paymentService.confirmPayment(request.getOrderId(), "COD");
            return ResponseEntity.ok(ApiResponse.success("Xác nhận thanh toán COD thành công", result));
        } catch (RuntimeException e) {
            log.warn("COD payment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/payos/create-link")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPayOSPaymentLink(
            Principal principal,
            @RequestBody ProcessPaymentRequest request) {
        try {
            var order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            // Kiểm tra nếu đơn hàng đã có PayOS order code (thanh toán lại)
            if (order.getPayosOrderCode() != null) {
                // Cancel payment link cũ trước khi tạo mới
                try {
                    payOSService.cancelPaymentLink(order.getPayosOrderCode());
                    log.info("Cancelled old PayOS payment link for order: {}", order.getId());
                } catch (Exception e) {
                    log.warn("Could not cancel old PayOS link (may already expired): {}", e.getMessage());
                }
                // Reset trạng thái PayOS của đơn hàng
                order.setPayosOrderCode(null);
                order.setPayosCheckoutUrl(null);
            }
            
            Map<String, Object> paymentInfo = paymentService.getPaymentInfo(request.getOrderId());
            
            Long orderId = (Long) paymentInfo.get("orderId");
            String orderNumber = (String) paymentInfo.get("orderNumber");
            var totalAmount = (java.math.BigDecimal) paymentInfo.get("totalAmount");
            String buyerName = (String) paymentInfo.get("customerName");
            String buyerEmail = (String) paymentInfo.get("customerEmail");
            String buyerPhone = (String) paymentInfo.get("customerPhone");
            
            // Tạo payment link từ PayOS
            PayOSService.PaymentLinkResult linkResult = payOSService.createPaymentLinkWithId(
                    orderId, orderNumber, totalAmount, 
                    buyerName, buyerEmail, buyerPhone
            );
            
            // Cập nhật checkoutUrl vào đơn hàng
            order.setPayosCheckoutUrl(linkResult.checkoutUrl());
            order.setPayosOrderCode(Long.valueOf(linkResult.orderCode()));
            order.setPaymentMethod(ntfur.com.entity.Order.PaymentMethod.PAYTOS);
            orderRepository.save(order);

            // Tính thời gian hết hạn để hiển thị cho user
            java.time.Instant expiredInstant = java.time.Instant.ofEpochSecond(linkResult.expiredAt());
            java.time.LocalDateTime expiredDateTime = expiredInstant.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();
            String expiryTimeStr = expiredDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));

            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "checkoutUrl", linkResult.checkoutUrl(),
                    "orderId", orderId,
                    "orderNumber", orderNumber,
                    "amount", totalAmount,
                    "expiresAt", linkResult.expiredAt(),
                    "expiryTimeStr", expiryTimeStr
            )));
        } catch (RuntimeException e) {
            log.warn("PayOS payment link creation error: {}", e.getMessage());
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

    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody String webhookData) {
        try {
            log.info("Received PayOS webhook: {}", webhookData);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("PayOS webhook error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid webhook");
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

    /**
     * Reset PayOS payment để user có thể đổi phương thức thanh toán.
     * Khi user hủy QR PayOS và muốn chọn lại PayOS hoặc chuyển sang COD.
     */
    @PostMapping("/payos/reset/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPayOSPayment(
            Principal principal,
            @PathVariable Long orderId) {
        try {
            var order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            // Kiểm tra trạng thái đơn hàng có cho phép reset không
            if (order.getPaymentStatus() == ntfur.com.entity.Order.PaymentStatus.PAID) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Đơn hàng đã được thanh toán, không thể thay đổi"));
            }
            
            if (order.getStatus() == ntfur.com.entity.Order.OrderStatus.CANCELLED) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Đơn hàng đã bị hủy"));
            }
            
            // Hủy PayOS link cũ nếu có
            if (order.getPayosOrderCode() != null) {
                try {
                    payOSService.cancelPaymentLink(order.getPayosOrderCode());
                    log.info("Reset PayOS - cancelled old payment link for order: {}", orderId);
                } catch (Exception e) {
                    log.warn("Reset PayOS - could not cancel old link (may expired): {}", e.getMessage());
                }
            }
            
            // Reset PayOS fields
            order.setPayosOrderCode(null);
            order.setPayosCheckoutUrl(null);
            orderRepository.save(order);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "orderId", orderId,
                    "message", "Đã reset PayOS. Bạn có thể chọn phương thức thanh toán khác."
            )));
        } catch (RuntimeException e) {
            log.warn("Reset PayOS payment error: {}", e.getMessage());
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
