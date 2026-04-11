package ntfur.com.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

/**
 * Service xử lý thanh toán qua payOS.
 * Tham khảo: https://payos.vn/docs/sdks/back-end/java/
 * API Docs: https://payos.vn/docs/api/#tag/payment-request/operation/payment-request
 */
@Service
@Slf4j
public class PayOSService {

    // Thời gian hết hạn link thanh toán (mặc định 10 phút = 600 giây)
    private static final int DEFAULT_EXPIRY_SECONDS = 600;

    private final PayOS payOS;
    private final String returnUrl;
    private final String cancelUrl;
    private final int expirySeconds;

    public PayOSService(
            @Value("${payos.client-id}") String clientId,
            @Value("${payos.api-key}") String apiKey,
            @Value("${payos.checksum-key}") String checksumKey,
            @Value("${payos.return-url}") String returnUrl,
            @Value("${payos.cancel-url}") String cancelUrl,
            @Value("${payos.expiry-seconds:600}") int expirySeconds) {

        this.payOS = new PayOS(clientId, apiKey, checksumKey);
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.expirySeconds = expirySeconds;
        log.info("PayOS service initialized - expiry: {} seconds", this.expirySeconds);
    }

    /**
     * Tạo payment link cho đơn hàng với thời gian hết hạn.
     *
     * @param orderId ID đơn hàng (dùng làm orderCode PayOS để dễ mapping)
     * @param orderNumber Số đơn hàng hiển thị (NTF-YYYYMMDD-XXXXXX)
     * @param amount Số tiền thanh toán
     * @param buyerName Tên người mua
     * @param buyerEmail Email người mua
     * @param buyerPhone SĐT người mua
     * @return PaymentLinkResult chứa checkoutUrl và orderCode
     */
    public PaymentLinkResult createPaymentLink(Long orderId, String orderNumber, BigDecimal amount,
            String buyerName, String buyerEmail, String buyerPhone) {
        long finalAmount = Math.max(amount.longValue(), 1000L); // Tối thiểu 1000 VND
        // Description hiển thị mã đơn hàng web để user dễ nhận biết
        String description = "NTF " + orderNumber;

        // Tính thời gian hết hạn (Unix Timestamp Int32)
        long currentTimestamp = Instant.now().getEpochSecond();
        long expiredAt = currentTimestamp + expirySeconds;
        log.info("PayOS - Link expires at Unix timestamp: {} ({} seconds from now)", expiredAt, expirySeconds);

        // Tạo orderCode mới dựa trên orderId và timestamp để tránh trùng lặp
        // PayOS yêu cầu orderCode là số nguyên dương duy nhất
        // Format: timestamp (9 chữ số) + 4 chữ số cuối của orderId
        long orderCode = (currentTimestamp % 1000000000L) * 10000L + (orderId % 10000L);

        log.info("PayOS - Creating payment link: orderId={}, orderNumber={}, orderCode={}, amount={}, expiredAt={}",
                orderId, orderNumber, orderCode, finalAmount, expiredAt);

        try {
            // Xây dựng request theo PayOS API
            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(finalAmount) // Số tiền (long)
                    .description(description)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .buyerName(buyerName)
                    .buyerEmail(buyerEmail)
                    .buyerPhone(buyerPhone)
                    .expiredAt(expiredAt) // Unix timestamp Int32
                    .build();

            // Gọi API tạo payment link
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentRequest);

            String checkoutUrl = response.getCheckoutUrl();
            log.info("PayOS - Payment link created: orderCode={}, checkoutUrl={}", orderCode, checkoutUrl);

            return new PaymentLinkResult(checkoutUrl, orderCode, expiredAt);

        } catch (Exception e) {
            log.error("PayOS - Failed to create payment link: orderCode={}, error={}", orderCode, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo liên kết thanh toán PayOS: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo payment link đơn giản (không cần thông tin người mua).
     */
    public PaymentLinkResult createPaymentLinkWithId(Long orderId, String orderNumber, BigDecimal amount) {
        return createPaymentLink(orderId, orderNumber, amount, null, null, null);
    }

    /**
     * Tạo payment link với thông tin người mua.
     */
    public PaymentLinkResult createPaymentLinkWithId(Long orderId, String orderNumber, BigDecimal amount,
            String buyerName, String buyerEmail, String buyerPhone) {
        return createPaymentLink(orderId, orderNumber, amount, buyerName, buyerEmail, buyerPhone);
    }

    /**
     * Lấy payment link đã tồn tại cho orderId.
     * @param orderId ID đơn hàng (dùng làm orderCode trong PayOS)
     * @return checkoutUrl nếu tồn tại, null nếu chưa có
     */
    public String getExistingPaymentUrl(Long orderId) {
        try {
            if (orderId == null) return null;
            var response = payOS.paymentRequests().get(orderId);
            if (response != null) {
                return response.toString(); // fallback
            }
        } catch (Exception e) {
            log.debug("PayOS - No existing payment link for orderId: {}", orderId);
        }
        return null;
    }

    /**
     * Hủy payment link đã tạo.
     * @param orderCode Mã order code từ PayOS
     * @return true nếu hủy thành công
     */
    public boolean cancelPaymentLink(long orderCode) {
        try {
            log.info("PayOS - Cancelling payment link: orderCode={}", orderCode);
            var response = payOS.paymentRequests().cancel(orderCode, "User cancelled");
            log.info("PayOS - Payment link cancelled: orderCode={}, status={}", orderCode, response.getStatus());
            return true;
        } catch (Exception e) {
            log.warn("PayOS - Failed to cancel payment link: orderCode={}, error={}", orderCode, e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thông tin payment link.
     * @param orderCode Mã order code từ PayOS
     * @return Thông tin payment link hoặc null nếu lỗi
     */
    public String getPaymentLinkStatus(long orderCode) {
        try {
            var response = payOS.paymentRequests().get(orderCode);
            return response.toString();
        } catch (Exception e) {
            log.debug("PayOS - Failed to get payment link status: orderCode={}", orderCode);
            return null;
        }
    }

    /**
     * Result object chứa checkoutUrl, orderCode và thời gian hết hạn từ PayOS.
     */
    public record PaymentLinkResult(String checkoutUrl, long orderCode, long expiredAt) {}

    /**
     * Result object cho backward compatibility (không có expiredAt).
     */
    public record PaymentLinkResultSimple(String checkoutUrl, long orderCode) {}
}
