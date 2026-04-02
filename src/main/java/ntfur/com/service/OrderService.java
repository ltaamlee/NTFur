package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Order;
import ntfur.com.entity.Order.OrderStatus;
import ntfur.com.entity.OrderItem;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.entity.dto.ShippingDTO;
import ntfur.com.entity.dto.OrderItemDTO;
import ntfur.com.repository.OrderItemRepository;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ShippingRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingRepository shippingRepository;
    
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + id));
        return toDTO(order);
    }

    public OrderDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với số: " + orderNumber));
        return toDTO(order);
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.valueOf(status)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> searchOrders(String keyword) {
        return orderRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getRecentOrders(int limit) {
        return orderRepository.findLatestOrders().stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + id));

        OrderStatus newStatus = OrderStatus.valueOf(status);
        order.setStatus(newStatus);

        switch (newStatus) {
            case CONFIRMED:
                order.setConfirmedAt(LocalDateTime.now());
                break;
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
            default:
                break;
        }

        Order updatedOrder = orderRepository.save(order);
        return toDTO(updatedOrder);
    }

    @Transactional
    public OrderDTO updatePaymentStatus(Long id, String paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + id));
        
        order.setPaymentStatus(Order.PaymentStatus.valueOf(paymentStatus));
        Order updatedOrder = orderRepository.save(order);
        return toDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đơn hàng với id: " + id);
        }
        orderRepository.deleteById(id);
    }

    public long countOrders() {
        return orderRepository.count();
    }

    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(OrderStatus.valueOf(status));
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenueByPaymentStatus(Order.PaymentStatus.PAID);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = orderRepository.getRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public List<Object[]> getOrderStatusCounts() {
        return orderRepository.countOrdersByStatus();
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems() != null ? 
            order.getItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList()) : new ArrayList<>();

        ShippingDTO shippingDTO = null;
        if (order.getShipping() != null) {
            shippingDTO = ShippingDTO.builder()
                    .id(order.getShipping().getId())
                    .orderId(order.getShipping().getOrder().getId())
                    .orderNumber(order.getOrderNumber())
                    .trackingNumber(order.getShipping().getTrackingNumber())
                    .carrier(order.getShipping().getCarrier())
                    .shippingMethod(order.getShipping().getShippingMethod() != null ? order.getShipping().getShippingMethod().name() : null)
                    .shippingFee(order.getShipping().getShippingFee())
                    .estimatedDays(order.getShipping().getEstimatedDays())
                    .status(order.getShipping().getStatus() != null ? order.getShipping().getStatus().name() : null)
                    .pickupDate(order.getShipping().getPickupDate())
                    .pickupTime(order.getShipping().getPickupTime())
                    .deliveryDate(order.getShipping().getDeliveryDate())
                    .deliveryTime(order.getShipping().getDeliveryTime())
                    .receiverName(order.getShipping().getReceiverName())
                    .receiverPhone(order.getShipping().getReceiverPhone())
                    .deliveryAddress(order.getShipping().getDeliveryAddress())
                    .deliveryNotes(order.getShipping().getDeliveryNotes())
                    .installationRequired(order.getShipping().isInstallationRequired())
                    .installationFee(order.getShipping().getInstallationFee())
                    .installationCompletedAt(order.getShipping().getInstallationCompletedAt())
                    .installationNotes(order.getShipping().getInstallationNotes())
                    .attemptCount(order.getShipping().getAttemptCount())
                    .lastAttemptDate(order.getShipping().getLastAttemptDate())
                    .failureReason(order.getShipping().getFailureReason())
                    .signedBy(order.getShipping().getSignedBy())
                    .signatureUrl(order.getShipping().getSignatureUrl())
                    .photoUrl(order.getShipping().getPhotoUrl())
                    .totalFee(order.getShipping().getTotalFee())
                    .createdAt(order.getShipping().getCreatedAt())
                    .updatedAt(order.getShipping().getUpdatedAt())
                    .build();
        }

        String customerName = order.getCustomer() != null ? order.getCustomer().getFullName() : 
                              order.getShippingFullName();
        String customerPhone = order.getCustomer() != null ? order.getCustomer().getPhone() : 
                               order.getShippingPhone();

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .shippingFullName(order.getShippingFullName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingDistrict(order.getShippingDistrict())
                .shippingWard(order.getShippingWard())
                .shippingNotes(order.getShippingNotes())
                .shippingFullAddress(order.getShippingFullAddress())
                .couponCode(order.getCouponCode())
                .couponDiscount(order.getCouponDiscount())
                .items(items)
                .shipping(shippingDTO)
                .orderDate(order.getOrderDate())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .originalPrice(item.getOriginalPrice())
                .quantity(item.getQuantity())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .total(item.getTotal())
                .weight(item.getWeight())
                .dimensions(item.getDimensions())
                .color(item.getColor())
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : null)
                .build();
    }
}
