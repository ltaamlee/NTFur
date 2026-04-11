package ntfur.com.controller.customer;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.Customer;
import ntfur.com.entity.Order;
import ntfur.com.entity.OrderItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.User;
import ntfur.com.exception.OutOfStockException;
import ntfur.com.exception.UnsupportedAreaException;
import ntfur.com.exception.ValidationException;
import org.springframework.transaction.annotation.Transactional;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.CustomerAddressDTO;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.UserRepository;
import ntfur.com.service.CustomerAddressService;
import ntfur.com.service.OrderService;
import ntfur.com.service.PaymentService;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerPortalController {

    private static final Set<String> SUPPORTED_CITIES = Set.of(
            "hồ chí minh", "ho chi minh", "tp.hcm", "tphcm", "tp hcm",
            "hcm", "hà nội", "ha noi", "hn", "đà nẵng", "da nang", "dn",
            "cần thơ", "can tho", "ct", "hải phòng", "hai phong", "hp",
            "đồng nai", "dong nai", "bình dương", "binh duong", "bd"
    );

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CustomerAddressService customerAddressService;
    private final PaymentService paymentService;

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
        profile.put("dateOfBirth", customer.getDateOfBirth());
        profile.put("gender", customer.getGender() != null ? customer.getGender().name() : null);
        profile.put("notes", customer.getNotes());
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
        customer.setNotes(nullSafe(request.getNotes()));
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (!isBlank(request.getGender())) {
            try {
                customer.setGender(Customer.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // ignore invalid gender
            }
        }
        customerRepository.save(customer);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", Map.of("updated", true)));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(Principal principal) {
        User user = getCurrentUser(principal);
        List<OrderDTO> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelOrder(Principal principal, @PathVariable Long id) {
        User user = getCurrentUser(principal);
        // Kiểm tra đơn hàng có thuộc về user không
        orderService.getOrdersByUserId(user.getId()).stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        try {
            Map<String, Object> result = paymentService.cancelPayment(id);
            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công!", result));
        } catch (RuntimeException e) {
            log.warn("Cancel order error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Address Management
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddressDTO>>> getAddresses(Principal principal) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        List<CustomerAddressDTO> addresses = customerAddressService.getAddressesByCustomerId(customer.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> getAddressById(Principal principal, @PathVariable Long id) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        try {
            CustomerAddressDTO address = customerAddressService.getAddressById(id, customer.getId());
            return ResponseEntity.ok(ApiResponse.success(address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> createAddress(Principal principal, @RequestBody CustomerAddressDTO dto) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        try {
            CustomerAddressDTO address = customerAddressService.createAddress(customer.getId(), dto);
            return ResponseEntity.ok(ApiResponse.success("Thêm địa chỉ thành công", address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> updateAddress(Principal principal, @PathVariable Long id, @RequestBody CustomerAddressDTO dto) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        try {
            CustomerAddressDTO address = customerAddressService.updateAddress(id, customer.getId(), dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(Principal principal, @PathVariable Long id) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        try {
            customerAddressService.deleteAddress(id, customer.getId());
            return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}/default")
    public ResponseEntity<ApiResponse<CustomerAddressDTO>> setDefaultAddress(Principal principal, @PathVariable Long id) {
        User user = getCurrentUser(principal);
        Customer customer = ensureCustomer(user);
        try {
            CustomerAddressDTO address = customerAddressService.setDefaultAddress(id, customer.getId());
            return ResponseEntity.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công", address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
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
    @Transactional
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
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setPaymentMethod(null);

            // Đặt deadline thanh toán là 7 ngày kể từ khi đặt
            order.setPaymentDeadline(LocalDateTime.now().plusDays(7));

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
                Product product = productRepository.findById(line.product().getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
                int qty = line.quantity();
                if (product.getStock() < qty) {
                    throw new OutOfStockException(product.getName());
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

            // Trả về thông tin đơn hàng - KHÔNG bao gồm thanh toán
            // Thanh toán sẽ được xử lý riêng qua /api/payment/*
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", saved.getId());
            responseData.put("orderNumber", saved.getOrderNumber());
            responseData.put("totalAmount", saved.getTotalAmount());
            responseData.put("paymentDeadline", saved.getPaymentDeadline());
            responseData.put("message", "Đặt hàng thành công! Vui lòng thanh toán trong 7 ngày.");

            return ResponseEntity.ok(ApiResponse.success(responseData));
        } catch (ValidationException | UnsupportedAreaException | OutOfStockException ex) {
            log.warn("Order validation error: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in createOrder: {}", ex.getMessage(), ex);
            throw new RuntimeException("Đặt hàng thất bại. Vui lòng thử lại.");
        }
    }

    private void validateShipping(CreateOrderRequest request) {
        // Validate required fields
        if (isBlank(request.getFullName())) {
            throw new ValidationException("fullName", "Vui lòng nhập họ tên");
        }
        if (isBlank(request.getPhone())) {
            throw new ValidationException("phone", "Vui lòng nhập số điện thoại");
        }
        if (!request.getPhone().matches("^(0|\\+84)[0-9]{9,10}$")) {
            throw new ValidationException("phone", "Số điện thoại không đúng định dạng (0xxx xxx xxx)");
        }
        if (isBlank(request.getAddress())) {
            throw new ValidationException("address", "Vui lòng nhập địa chỉ cụ thể");
        }
        if (isBlank(request.getCity())) {
            throw new ValidationException("city", "Vui lòng nhập tỉnh/thành phố");
        }
        if (!isSupportedArea(request.getCity())) {
            throw new UnsupportedAreaException("Rất tiếc, khu vực này hiện chưa có dịch vụ giao hàng");
        }
    }

    private boolean isSupportedArea(String city) {
        if (city == null) return false;
        String c = city.toLowerCase().trim();
        // Check exact match in supported cities
        if (SUPPORTED_CITIES.contains(c)) {
            return true;
        }
        // Check partial match for variations
        for (String supported : SUPPORTED_CITIES) {
            if (c.contains(supported) || supported.contains(c)) {
                return true;
            }
        }
        return false;
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
                throw new OutOfStockException(product.getName());
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
        private LocalDate dateOfBirth;
        private String gender;
        private String notes;
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
        private List<CartItemRequest> items;
    }
}
