package ntfur.com.controller.paymentgateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.service.OrderService;

/**
 * Controller xử lý redirect từ PayOS (returnUrl).
 * PayOS sẽ redirect user về trang này sau khi thanh toán xong.
 */
@Controller
@RequestMapping("/payment/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSRedirectViewController {

    private final OrderService orderService;

    /**
     * Xử lý redirect từ PayOS.
     * URL mẫu: /payment/payos/return?code=00&id=xxx&cancel=false&status=PAID&orderCode=7760248940005
     */
    @GetMapping("/return")
    public String handlePayOSReturn(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cancel,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String desc) {

        log.info("=== PayOS RETURN ===");
        log.info("orderCode: [{}], status: [{}], cancel: [{}], code: [{}], desc: [{}]",
                orderCode, status, cancel, code, desc);

        // Kiểm tra thanh toán thành công: code="00" hoặc cancel=false với code="00"
        boolean isSuccess = "00".equals(code)
                || ("false".equalsIgnoreCase(cancel) && "00".equals(code))
                || "PAID".equalsIgnoreCase(status);

        if (isSuccess && orderCode != null && !orderCode.trim().isEmpty()) {
            try {
                long codeLong = Long.parseLong(orderCode.trim());
                log.info("Confirming PayOS payment for orderCode: {}", codeLong);
                orderService.confirmPayOSPayment(codeLong);
                log.info("Payment confirmed successfully!");
                return "redirect:/orders?payment=success";
            } catch (NumberFormatException e) {
                log.error("Invalid orderCode format: [{}]", orderCode, e);
                return "redirect:/orders?payment=error";
            } catch (Exception e) {
                log.error("Failed to confirm payment for orderCode: [{}]", orderCode, e);
                return "redirect:/orders?payment=error";
            }
        }

        // Thanh toán thất bại hoặc bị hủy
        log.info("Payment failed or cancelled. Redirecting to /orders?payment=cancelled");
        return "redirect:/orders?payment=cancelled";
    }

    /**
     * Xử lý khi user hủy thanh toán.
     */
    @GetMapping("/cancel")
    public String handlePayOSCancel(@RequestParam(required = false) String orderCode) {
        log.info("PayOS cancelled - orderCode: {}", orderCode);
        return "redirect:/orders?payment=cancelled";
    }

    /**
     * Endpoint debug để kiểm tra return URL parameters.
     */
    @GetMapping("/debug")
    @ResponseBody
    public Map<String, String> debug(@RequestParam Map<String, String> params) {
        log.info("PayOS debug - all params: {}", params);
        return params;
    }
}