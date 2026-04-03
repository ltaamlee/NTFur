package ntfur.com.controller.customer;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Customer;
import ntfur.com.entity.Order;
import ntfur.com.entity.OrderItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.User;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.UserRepository;
import ntfur.com.service.OrderService;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerPortalController {

    private static final String VIETQR_BANK = "MB";
    private static final String VIETQR_ACCOUNT = "0856006888";
    private static final String VIETQR_ACCOUNT_NAME = "NTFurniture";

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(Principal principal) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("fullName", customer.getFullName() != null ? customer.getFullName() : user.getFullName());
        profile.put("phone", customer.getPhone() != null ? customer.getPhone() : user.getPhone());
        profile.put("address", customer.getAddress());
        profile.put("city", customer.getCity());
        profile.put("district", customer.getDistrict());
        profile.put("ward", customer.getWard());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("totalOrders", customer.getTotalOrders());
        profile.put("totalSpent", customer.getTotalSpent());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(Principal principal,
                                                                          @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);

        if (isBlank(request.getFullName()) || isBlank(request.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Họ tên và số điện thoại là bắt buộc"));
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        if (!isBlank(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        userRepository.save(user);

        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone().trim());
        customer.setAddress(nullSafe(request.getAddress()));
        customer.setCity(nullSafe(request.getCity()));
        customer.setDistrict(nullSafe(request.getDistrict()));
        customer.setWard(nullSafe(request.getWard()));
        customerRepository.save(customer);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", Map.of("updated", true)));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(Principal principal) {
        User user = getCurrentUser(principal);
        List<OrderDTO> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewOrder(@RequestBody CreateOrderRequest request) {
        try {
            validateShipping(request);
            List<CartLine> normalized = normalizeAndCheckStock(request.getItems());
            BigDecimal subtotal = calcSubtotal(normalized);
            BigDecimal shippingFee = calculateShippingFee(request.getCity(), normalized);
            BigDecimal total = subtotal.add(shippingFee);
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "subtotal", subtotal,
                    "shippingFee", shippingFee,
                    "discountAmount", BigDecimal.ZERO,
                    "totalAmount", total
            )));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(Principal principal,
                                                                        @RequestBody CreateOrderRequest request) {
        try {
            User user = getCurrentUser(principal);
            Customer customer = ensureCustomer(user);
            validateShipping(request);
            List<CartLine> normalized = normalizeAndCheckStock(request.getItems());

            Order order = new Order();
            order.setUser(user);
            order.setCustomer(customer);
            order.setStatus(Order.OrderStatus.PENDING);

            String paymentMethod = request.getPaymentMethod() == null ? "COD" : request.getPaymentMethod().toUpperCase();
            if ("ONLINE".equals(paymentMethod)) {
                order.setPaymentMethod(Order.PaymentMethod.BANK_TRANSFER);
                order.setPaymentStatus(Order.PaymentStatus.PENDING);
            } else {
                order.setPaymentMethod(Order.PaymentMethod.COD);
                order.setPaymentStatus(Order.PaymentStatus.PENDING);
            }

            order.setShippingFullName(request.getFullName().trim());
            order.setShippingPhone(request.getPhone().trim());
            order.setShippingAddress(request.getAddress().trim());
            order.setShippingCity(request.getCity().trim());
            order.setShippingDistrict(nullSafe(request.getDistrict()));
            order.setShippingWard(nullSafe(request.getWard()));
            order.setShippingNotes(nullSafe(request.getNotes()));

            BigDecimal subtotal = calcSubtotal(normalized);
            BigDecimal shippingFee = calculateShippingFee(request.getCity(), normalized);
            order.setSubtotal(subtotal);
            order.setShippingFee(shippingFee);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setTaxAmount(BigDecimal.ZERO);

            List<OrderItem> orderItems = new ArrayList<>();
            for (CartLine line : normalized) {
                Product product = line.product();
                int qty = line.quantity();
                if (product.getStock() < qty) {
                    throw new RuntimeException("Rất tiếc, sản phẩm " + product.getName() + " vừa hết hàng");
                }
                product.setStock(product.getStock() - qty);
                productRepository.save(product);

                OrderItem item = new OrderItem(product, qty);
                item.setOrder(order);
                item.calculateTotal();
                orderItems.add(item);
            }

            order.setItems(orderItems);
            order.calculateTotal();
            Order saved = orderRepository.save(order);

            customer.setTotalOrders(customer.getTotalOrders() + 1);
            customer.setTotalSpent(customer.getTotalSpent().add(saved.getTotalAmount()));
            customerRepository.save(customer);

            String qrUrl = null;
            if (saved.getPaymentMethod() == Order.PaymentMethod.BANK_TRANSFER) {
                qrUrl = buildVietQrUrl(saved.getTotalAmount(), saved.getOrderNumber());
            }

            return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công!", Map.of(
                    "orderId", saved.getId(),
                    "orderNumber", saved.getOrderNumber(),
                    "totalAmount", saved.getTotalAmount(),
                    "paymentMethod", saved.getPaymentMethod().name(),
                    "vietQrUrl", qrUrl
            )));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    private String buildVietQrUrl(BigDecimal amount, String orderNumber) {
        String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
        String addInfo = URLEncoder.encode("Thanh toan don " + orderNumber, StandardCharsets.UTF_8);
        String accountName = URLEncoder.encode(VIETQR_ACCOUNT_NAME, StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/" + VIETQR_BANK + "-" + VIETQR_ACCOUNT
                + "-compact2.png?amount=" + amountStr + "&addInfo=" + addInfo + "&accountName=" + accountName;
    }

    private void validateShipping(CreateOrderRequest request) {
        if (isBlank(request.getFullName()) || isBlank(request.getPhone()) || isBlank(request.getAddress()) || isBlank(request.getCity())) {
            throw new RuntimeException("Vui lòng nhập đầy đủ thông tin giao hàng bắt buộc");
        }
        if (!request.getPhone().matches("^(0|\\+84)[0-9]{9,10}$")) {
            throw new RuntimeException("Số điện thoại không đúng định dạng");
        }
        if (!isSupportedArea(request.getCity())) {
            throw new RuntimeException("Rất tiếc, khu vực này hiện chưa có dịch vụ giao hàng");
        }
    }

    private boolean isSupportedArea(String city) {
        String c = city.toLowerCase();
        return c.contains("hồ chí minh") || c.contains("ho chi minh") || c.contains("tp.hcm");
    }

    private BigDecimal calculateShippingFee(String city, List<CartLine> items) {
        int totalQty = items.stream().mapToInt(CartLine::quantity).sum();
        BigDecimal base = new BigDecimal("30000");
        BigDecimal extra = new BigDecimal(Math.max(0, totalQty - 1) * 5000L);
        return base.add(extra);
    }

    private List<CartLine> normalizeAndCheckStock(List<CartItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Giỏ hàng phải có ít nhất 1 sản phẩm hợp lệ");
        }
        List<CartLine> lines = new ArrayList<>();
        for (CartItemRequest item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Sản phẩm không tồn tại");
            }
            Product product = productOpt.get();
            if (product.getStock() <= 0 || product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Rất tiếc, sản phẩm " + product.getName() + " vừa hết hàng");
            }
            lines.add(new CartLine(product, item.getQuantity()));
        }
        if (lines.isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm hợp lệ");
        }
        return lines;
    }

    private BigDecimal calcSubtotal(List<CartLine> lines) {
        return lines.stream()
                .map(line -> line.product().getPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chức năng này");
        }
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Customer ensureCustomer(User user) {
        return customerRepository.findByUserId(user.getId()).orElseGet(() -> {
            Customer c = new Customer();
            c.setUser(user);
            c.setFullName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            c.setPhone(user.getPhone());
            c.setTotalSpent(BigDecimal.ZERO);
            c.setTotalOrders(0);
            return customerRepository.save(c);
        });
    }

    private String nullSafe(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record CartLine(Product product, int quantity) {}

    @Data
    static class UpdateProfileRequest {
        private String fullName;
        private String phone;
        private String address;
        private String city;
        private String district;
        private String ward;
        private String avatarUrl;
    }

    @Data
    static class CartItemRequest {
        private Long productId;
        private Integer quantity;
    }

    @Data
    static class CreateOrderRequest {
        private String fullName;
        private String phone;
        private String address;
        private String city;
        private String district;
        private String ward;
        private String notes;
        private String paymentMethod; // COD | ONLINE
        private List<CartItemRequest> items;
    }
}
