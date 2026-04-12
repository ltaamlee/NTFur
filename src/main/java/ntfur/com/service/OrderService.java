package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Customer;
import ntfur.com.entity.Order;
import ntfur.com.entity.Order.OrderStatus;
import ntfur.com.entity.OrderItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.Warehouse;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.entity.dto.OrderItemDTO;
import ntfur.com.entity.dto.ShippingDTO;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.OrderItemRepository;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.ShippingRepository;
import ntfur.com.repository.WarehouseRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingRepository shippingRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseService warehouseService;
    
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

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
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

    /**
     * Tạo đơn hàng mới - theo đặc tả chức năng Đặt hàng
     * - Kiểm tra tồn kho
     * - Tạo đơn hàng với thông tin shipping
     * - Cập nhật tồn kho
     * - Trả về mã đơn hàng
     */
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        // ========== VALIDATION ==========
        // Kiểm tra danh sách sản phẩm không rỗng (bước 1-2)
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không có sản phẩm để đặt");
        }

        // Bước 2: Kiểm tra tồn kho cho tất cả sản phẩm
        List<String> outOfStockProducts = new ArrayList<>();
        List<String> lowStockProducts = new ArrayList<>();

        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            if (itemDTO.getProductId() == null) {
                throw new RuntimeException("Mã sản phẩm không hợp lệ");
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong hệ thống"));

            int requestedQuantity = itemDTO.getQuantity() > 0 ? itemDTO.getQuantity() : 1;
            int availableStock = product.getStock();

            if (availableStock <= 0) {
                outOfStockProducts.add(product.getName());
            } else if (availableStock < requestedQuantity) {
                lowStockProducts.add(product.getName() + " (còn " + availableStock + ")");
            }
        }

        // Nếu có sản phẩm hết hàng - ném exception (tình huống thay thế c)
        if (!outOfStockProducts.isEmpty()) {
            throw new RuntimeException("Rất tiếc, sản phẩm " + String.join(", ", outOfStockProducts) + " đã hết hàng");
        }

        // Cảnh báo nếu có sản phẩm số lượng không đủ
        if (!lowStockProducts.isEmpty()) {
            // Có thể xử lý thêm nếu cần - tạm thời cho phép đặt với số lượng tồn kho
        }

        // ========== TẠO ĐƠN HÀNG ==========
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setShippingFullName(orderDTO.getShippingFullName());
        order.setShippingPhone(orderDTO.getShippingPhone());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setShippingCity(orderDTO.getShippingCity());
        order.setShippingDistrict(orderDTO.getShippingDistrict());
        order.setShippingWard(orderDTO.getShippingWard());
        order.setShippingNotes(orderDTO.getShippingNotes());
        order.setPaymentMethod(Order.PaymentMethod.valueOf(
                orderDTO.getPaymentMethod() != null ? orderDTO.getPaymentMethod() : "COD"));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        // Liên kết customer/user nếu có
        if (orderDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(orderDTO.getCustomerId()).orElse(null);
            order.setCustomer(customer);
            if (customer != null && customer.getUser() != null) {
                order.setUser(customer.getUser());
            }
        } else if (orderDTO.getUserId() != null) {
            customerRepository.findById(orderDTO.getUserId())
                    .ifPresent(customer -> {
                        order.setCustomer(customer);
                        order.setUser(customer.getUser());
                    });
        }

        // Tính phí vận chuyển (bước 6 - tạm thời set = 0, có thể mở rộng sau)
        BigDecimal shippingFee = orderDTO.getShippingFee() != null ? orderDTO.getShippingFee() : BigDecimal.ZERO;
        order.setShippingFee(shippingFee);

        // Tính subtotal và total
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId()).orElse(null);
            int quantity = itemDTO.getQuantity() > 0 ? itemDTO.getQuantity() : 1;

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(itemDTO.getProductName() != null ? itemDTO.getProductName() : product.getName());
            item.setProductSku(itemDTO.getProductSku() != null ? itemDTO.getProductSku() : product.getSku());
            item.setProductImage(itemDTO.getProductImage() != null ? itemDTO.getProductImage() : product.getMainImage());
            item.setPrice(itemDTO.getPrice() != null ? itemDTO.getPrice() : product.getPrice());
            item.setQuantity(quantity);
            item.setColor(itemDTO.getColor());
            item.setDimensions(itemDTO.getDimensions());
            item.calculateTotal();

            order.getItems().add(item);
            subtotal = subtotal.add(item.getTotal());
        }

        order.setSubtotal(subtotal);
        order.calculateTotal();

        // Coupon discount
        if (orderDTO.getCouponCode() != null && !orderDTO.getCouponCode().isEmpty()) {
            order.setCouponCode(orderDTO.getCouponCode());
            order.setCouponDiscount(orderDTO.getCouponDiscount() != null ? orderDTO.getCouponDiscount() : BigDecimal.ZERO);
            order.calculateTotal();
        }

        Order savedOrder = orderRepository.save(order);

        // ========== CẬP NHẬT TỒN KHO (bước 9) ==========
        // Ưu tiên lấy từ Warehouse, fallback về Product.stock
        List<Warehouse> activeWarehouses = warehouseRepository.findAllActiveWarehouses();
        
        for (OrderItem item : savedOrder.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                
                if (!activeWarehouses.isEmpty()) {
                    Warehouse warehouse = activeWarehouses.get(0);
                    try {
                        // Thử xuất kho từ Warehouse trước
                        warehouseService.exportStock(warehouse.getId(), product.getId(), item.getQuantity(),
                                "Xuất kho cho đơn hàng: " + savedOrder.getOrderNumber(), "System");
                        continue; // Thành công, bỏ qua
                    } catch (Exception e) {
                        // Warehouse không đủ hàng, fallback về Product.stock
                    }
                }
                
                // Fallback: Giảm trực tiếp trên Product
                product.setStock(product.getStock() - item.getQuantity());
                if (product.getStock() <= 0) {
                    product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
                }
                productRepository.save(product);
            }
        }

        return toDTO(savedOrder);
    }

    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth());
        int randomPart = (int) (Math.random() * 900000) + 100000;
        return String.format("NTF-%s-%d", datePart, randomPart);
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

    /**
     * Xác nhận thanh toán PayOS thành công.
     * @param orderCode Mã orderCode từ PayOS (đã lưu trong Order.payosOrderCode)
     * @return OrderDTO đã cập nhật
     */
    @Transactional
    public OrderDTO confirmPayOSPayment(long orderCode) {
        log.info("confirmPayOSPayment called with orderCode: {}", orderCode);

        Order order = orderRepository.findByPayosOrderCode(orderCode)
                .orElse(null);

        if (order == null) {
            log.error("Cannot find order with payosOrderCode: {}. Searching all orders...", orderCode);
            // Debug: List all orders with payosOrderCode
            List<Order> allOrders = orderRepository.findAll();
            for (Order o : allOrders) {
                log.info("Order: id={}, orderNumber={}, payosOrderCode={}, paymentStatus={}",
                        o.getId(), o.getOrderNumber(), o.getPayosOrderCode(), o.getPaymentStatus());
                if (o.getPayosOrderCode() != null) {
                    log.info("Found order with matching payosOrderCode range: {}", o.getPayosOrderCode());
                }
            }
            throw new RuntimeException("Không tìm thấy đơn hàng với PayOS orderCode: " + orderCode);
        }

        log.info("Found order: id={}, orderNumber={}, currentPaymentStatus={}",
                order.getId(), order.getOrderNumber(), order.getPaymentStatus());

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            log.info("Order {} already paid", order.getOrderNumber());
            return toDTO(order);
        }

        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("Payment confirmed for order: {} (id={})", updatedOrder.getOrderNumber(), updatedOrder.getId());

        return toDTO(updatedOrder);
    }

    /**
     * Cập nhật mã PayOS cho đơn hàng.
     */
    @Transactional
    public void updatePayOSOrderCode(Long orderId, long payosOrderCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));
        
        order.setPayosOrderCode(payosOrderCode);
        orderRepository.save(order);
        log.info("Updated PayOS orderCode for order {}: {}", order.getOrderNumber(), payosOrderCode);
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
        String customerEmail = order.getCustomer() != null ? order.getCustomer().getUser().getEmail() : null;

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerEmail(customerEmail)
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
                .payosOrderCode(order.getPayosOrderCode())
                .payosCheckoutUrl(order.getPayosCheckoutUrl())
                .paymentDeadline(order.getPaymentDeadline())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        // Build variant info from color and dimensions
        String variantInfo = null;
        if (item.getColor() != null || item.getDimensions() != null) {
            StringBuilder sb = new StringBuilder();
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                sb.append("Màu: ").append(item.getColor());
            }
            if (item.getDimensions() != null && !item.getDimensions().isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Kích thước: ").append(item.getDimensions());
            }
            variantInfo = sb.toString();
        }

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
                .variantInfo(variantInfo)
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : null)
                .build();
    }
}
