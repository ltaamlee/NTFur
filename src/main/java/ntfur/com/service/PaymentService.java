package ntfur.com.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Order;
import ntfur.com.entity.Order.PaymentMethod;
import ntfur.com.entity.Order.PaymentStatus;
import ntfur.com.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PayOSService payOSService;


    public Map<String, Object> getPaymentInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("totalAmount", order.getTotalAmount());
        result.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        result.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        result.put("paymentDeadline", order.getPaymentDeadline());
        result.put("isExpired", order.isPaymentExpired());
        
        // Thông tin khách hàng cho PayOS
        if (order.getUser() != null) {
            result.put("customerName", order.getUser().getFullName());
            result.put("customerEmail", order.getUser().getEmail());
            result.put("customerPhone", order.getUser().getPhone());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> initiatePayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã được thanh toán");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy");
        }

        if (order.isPaymentExpired()) {
            throw new RuntimeException("Đơn hàng đã quá hạn thanh toán");
        }

        // === COD: Đã xác nhận thì không cho đổi sang phương thức khác ===
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            throw new RuntimeException("Bạn đã xác nhận thanh toán COD. Không thể thay đổi phương thức thanh toán.");
        }

        // === PayOS: Cho phép đổi phương thức ===
        // Xóa link cũ nếu có (link có thể đã hết hạn)
        if (order.getPayosCheckoutUrl() != null || order.getPayosOrderCode() != null) {
            order.setPayosCheckoutUrl(null);
            order.setPayosOrderCode(null);
        }

        if ("PAYTOS".equalsIgnoreCase(paymentMethod) || "QR".equalsIgnoreCase(paymentMethod) || "BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
            return initiateQrPayment(order);
        } else if ("COD".equalsIgnoreCase(paymentMethod)) {
            return initiateCodPayment(order);
        } else {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        }
    }

    @Transactional
    public Map<String, Object> initiateQrPayment(Order order) {
        // Luôn tạo payment link mới để đảm bảo mã QR còn hạn
        order.setPaymentMethod(Order.PaymentMethod.PAYTOS);
        orderRepository.save(order);

        try {
            // Tạo payment link từ PayOS với thời gian hết hạn
            PayOSService.PaymentLinkResult linkResult = payOSService.createPaymentLinkWithId(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getTotalAmount()
            );

            // Lưu checkoutUrl và orderCode vào đơn hàng
            order.setPayosCheckoutUrl(linkResult.checkoutUrl());
            order.setPayosOrderCode(linkResult.orderCode());
            orderRepository.save(order);

            // Tính thời gian hết hạn để hiển thị cho user
            java.time.Instant expiredInstant = java.time.Instant.ofEpochSecond(linkResult.expiredAt());
            java.time.LocalDateTime expiredDateTime = expiredInstant.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();
            String expiryTimeStr = expiredDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("paymentMethod", "PAYTOS");
            result.put("paymentStatus", order.getPaymentStatus().name());
            result.put("checkoutUrl", linkResult.checkoutUrl());
            result.put("expiresAt", linkResult.expiredAt());
            result.put("expiryTimeStr", expiryTimeStr);
            result.put("message", "Vui lòng thanh toán trước " + expiryTimeStr);

            return result;
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link: {}", e.getMessage(), e);

            // Fallback: Nếu PayOS lỗi, vẫn cho phép thanh toán COD
            order.setPaymentMethod(Order.PaymentMethod.COD);
            orderRepository.save(order);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("paymentMethod", "COD");
            result.put("paymentStatus", order.getPaymentStatus().name());
            result.put("message", "PayOS không khả dụng. Đơn hàng chuyển sang thanh toán COD. Bạn sẽ thanh toán khi nhận hàng.");
            result.put("paymentDeadline", order.getPaymentDeadline());

            return result;
        }
    }

    @Transactional
    public Map<String, Object> initiateCodPayment(Order order) {
        order.setPaymentMethod(Order.PaymentMethod.COD);
        orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("paymentMethod", "COD");
        result.put("paymentStatus", order.getPaymentStatus().name());
        result.put("message", "Thanh toán khi nhận hàng. Vui lòng thanh toán trước " + order.getPaymentDeadline());
        result.put("paymentDeadline", order.getPaymentDeadline());

        return result;
    }

    @Transactional
    public Map<String, Object> confirmPayment(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã được thanh toán");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy");
        }

        // Cho phép đổi phương thức thanh toán - reset link cũ nếu có
        if (order.getPayosCheckoutUrl() != null || order.getPayosOrderCode() != null) {
            order.setPayosCheckoutUrl(null);
            order.setPayosOrderCode(null);
        }

        if ("COD".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentMethod(Order.PaymentMethod.COD);
            orderRepository.save(order);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("paymentMethod", "COD");
            result.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING");
            result.put("message", "Đã xác nhận thanh toán COD thành công. Bạn sẽ thanh toán khi nhận hàng.");

            return result;
        }

        // Các phương thức khác (QR/BANK_TRANSFER/PAYTOS): Xác nhận thanh toán
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("totalAmount", order.getTotalAmount());
        result.put("paymentStatus", "PAID");
        result.put("message", "Xác nhận thanh toán thành công");

        return result;
    }

    @Transactional
    public Map<String, Object> cancelPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép hủy khi đơn hàng còn ở trạng thái chờ xác nhận (PENDING)
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Không thể hủy đơn đã thanh toán");
        }

        // Hoàn lại stock cho từng sản phẩm
        order.getItems().forEach(item -> {
            item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
        });

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason("Khách hủy đơn hàng");
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("status", "CANCELLED");
        result.put("message", "Đơn hàng đã được hủy và sản phẩm đã được trả lại kho");

        return result;
    }
}
