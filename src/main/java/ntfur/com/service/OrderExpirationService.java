package ntfur.com.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Order;
import ntfur.com.entity.Order.OrderStatus;
import ntfur.com.entity.Order.PaymentStatus;
import ntfur.com.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationService {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cancelExpiredOrders() {
        log.info("Running scheduled task: cancel expired unpaid orders");

        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(now);

        if (expiredOrders.isEmpty()) {
            log.info("No expired orders found");
            return;
        }

        log.info("Found {} expired orders to cancel", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                cancelExpiredOrder(order);
            } catch (Exception e) {
                log.error("Failed to cancel order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }

        log.info("Completed cancellation of expired orders");
    }

    @Transactional
    public void cancelExpiredOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setCancellationReason("Đã quá hạn thanh toán (quá 7 ngày kể từ ngày đặt hàng)");
        order.setCancelledAt(LocalDateTime.now());

        order.getItems().forEach(item -> {
            item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
        });

        orderRepository.save(order);
        log.info("Cancelled expired order: {}", order.getOrderNumber());
    }

    public long countExpiredOrders() {
        return orderRepository.countExpiredPendingOrders(LocalDateTime.now());
    }
}