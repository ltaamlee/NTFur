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
import ntfur.com.entity.Order.PaymentStatus;
import ntfur.com.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String VIETQR_BANK = "MB";
    private static final String VIETQR_ACCOUNT = "0856006888";
    private static final String VIETQR_ACCOUNT_NAME = "NTFurniture";

    private final OrderRepository orderRepository;

    public String buildVietQrUrl(BigDecimal amount, String orderNumber) {
        String amountStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        String addInfo = URLEncoder.encode("Thanh toan don " + orderNumber, StandardCharsets.UTF_8);
        String accountName = URLEncoder.encode(VIETQR_ACCOUNT_NAME, StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/" + VIETQR_BANK + "-" + VIETQR_ACCOUNT
                + "-compact2.png?amount=" + amountStr + "&addInfo=" + addInfo + "&accountName=" + accountName;
    }

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

        if (order.getPaymentMethod() == Order.PaymentMethod.BANK_TRANSFER) {
            result.put("vietQrUrl", buildVietQrUrl(order.getTotalAmount(), order.getOrderNumber()));
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

        if ("QR".equalsIgnoreCase(paymentMethod) || "BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
            return initiateQrPayment(order);
        } else if ("COD".equalsIgnoreCase(paymentMethod)) {
            return initiateCodPayment(order);
        } else {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        }
    }

    @Transactional
    public Map<String, Object> initiateQrPayment(Order order) {
        order.setPaymentMethod(Order.PaymentMethod.BANK_TRANSFER);
        orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("vietQrUrl", buildVietQrUrl(order.getTotalAmount(), order.getOrderNumber()));
        result.put("paymentMethod", "BANK_TRANSFER");
        result.put("paymentStatus", order.getPaymentStatus().name());
        result.put("message", "Vui lòng quét mã QR để thanh toán trước " + order.getPaymentDeadline());
        result.put("paymentDeadline", order.getPaymentDeadline());

        return result;
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

        // Nếu là COD: Chỉ xác nhận phương thức, KHÔNG đánh dấu đã thanh toán
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentMethod(Order.PaymentMethod.COD);
            orderRepository.save(order);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("paymentMethod", "COD");
            result.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING");
            result.put("message", "Xác nhận phương thức COD thành công");

            return result;
        }

        // Các phương thức khác (QR/BANK_TRANSFER): Xác nhận thanh toán thực sự
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã được thanh toán");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy");
        }

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

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Không thể hủy đơn đã thanh toán");
        }

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
