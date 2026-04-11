package ntfur.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xử lý redirect từ PayOS sau khi thanh toán.
 * PayOS sẽ redirect user về returnUrl hoặc cancelUrl sau khi thanh toán.
 */
@Controller
@RequestMapping("/payment/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSRedirectController {

    /**
     * Xử lý redirect khi thanh toán thành công hoặc bị hủy.
     * PayOS sẽ gọi endpoint này với các tham số: orderCode, status, cancel
     */
    @GetMapping("/return")
    public String handlePayOSReturn(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cancel,
            @RequestParam(required = false) Long orderId) {
        
        log.info("PayOS return - orderCode: {}, status: {}, cancel: {}, orderId: {}", 
                orderCode, status, cancel, orderId);
        
        if (cancel != null || "cancel".equalsIgnoreCase(status)) {
            log.info("User cancelled PayOS payment for orderCode: {}", orderCode);
            return "redirect:/orders?payment=cancelled";
        }
        
        if ("success".equalsIgnoreCase(status)) {
            log.info("PayOS payment success for orderCode: {}, orderId: {}", orderCode, orderId);
            return "redirect:/orders?payment=success";
        }
        
        log.warn("PayOS return with unknown status: {}", status);
        return "redirect:/orders";
    }

    /**
     * Xử lý redirect khi user hủy thanh toán.
     */
    @GetMapping("/cancel")
    public String handlePayOSCancel(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Long orderId) {
        
        log.info("PayOS cancelled - orderCode: {}, orderId: {}", orderCode, orderId);
        return "redirect:/orders?payment=cancelled";
    }
}
