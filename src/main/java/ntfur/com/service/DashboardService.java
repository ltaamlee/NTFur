package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Order;
import ntfur.com.entity.OrderItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.Shipping;
import ntfur.com.entity.Shipping.ShippingMethod;
import ntfur.com.entity.Shipping.ShippingStatus;
import ntfur.com.entity.dto.DashboardDTO;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.entity.dto.RevenueDataPoint;
import ntfur.com.entity.dto.category.CategoryStatsDTO;
import ntfur.com.entity.dto.customer.CustomerDTO;
import ntfur.com.entity.dto.product.TopProductDTO;
import ntfur.com.repository.CategoryRepository;
import ntfur.com.repository.CustomerRepository;
import ntfur.com.repository.OrderItemRepository;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CategoryRepository categoryRepository;

    public DashboardDTO getDashboardData() {
        long totalProducts = productRepository.count();
        long totalCustomers = customerRepository.countTotalCustomers();
        long totalOrders = orderRepository.count();

        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long processingOrders = orderRepository.countByStatus(Order.OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        BigDecimal totalRevenue = getRevenueOrZero(orderRepository.getTotalRevenueByPaymentStatus(Order.PaymentStatus.PAID));
        
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.now().withDayOfYear(1).with(LocalTime.MIN);

        BigDecimal todayRevenue = getRevenueOrZero(orderRepository.getRevenueByDateRange(today, LocalDateTime.now()));
        BigDecimal monthRevenue = getRevenueOrZero(orderRepository.getRevenueByDateRange(monthStart, LocalDateTime.now()));
        BigDecimal yearRevenue = getRevenueOrZero(orderRepository.getRevenueByDateRange(yearStart, LocalDateTime.now()));

        List<RevenueDataPoint> revenueChart = getRevenueChartData();
        List<TopProductDTO> topProducts = getTopProducts(5);
        List<OrderDTO> recentOrders = getRecentOrdersDTO(5);
        List<CustomerDTO> recentCustomers = getRecentCustomersDTO(5);
        List<CategoryStatsDTO> categoryStats = getCategoryStats();

        return DashboardDTO.builder()
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .yearRevenue(yearRevenue)
                .revenueChart(revenueChart)
                .topProducts(topProducts)
                .recentOrders(recentOrders)
                .recentCustomers(recentCustomers)
                .categoryStats(categoryStats)
                .build();
    }

    public List<RevenueDataPoint> getRevenueChart(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = orderRepository.getDailyRevenue(startDate);
        
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        for (Object[] result : results) {
            String date = result[0] != null ? result[0].toString() : "";
            BigDecimal revenue = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
            dataPoints.add(RevenueDataPoint.builder()
                    .date(date)
                    .revenue(revenue)
                    .orderCount(1)
                    .build());
        }
        return dataPoints;
    }

    private List<RevenueDataPoint> getRevenueChartData() {
        return getRevenueChart(30);
    }

    private List<TopProductDTO> getTopProducts(int limit) {
        List<Object[]> results = orderItemRepository.findTopSellingProducts();
        
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Long productId = (Long) result[0];
                    String productName = (String) result[1];
                    Long totalSold = (Long) result[2];
                    
                    Product product = productRepository.findById(productId).orElse(null);
                    BigDecimal price = product != null && product.getPrice() != null ? 
                                       product.getPrice() : BigDecimal.ZERO;
                    BigDecimal totalRevenue = price.multiply(BigDecimal.valueOf(totalSold));
                    
                    return TopProductDTO.builder()
                            .id(productId)
                            .name(productName)
                            .sku(product != null ? product.getSku() : null)
                            .mainImage(product != null ? product.getMainImage() : null)
                            .price(price)
                            .totalSold(totalSold)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<OrderDTO> getRecentOrdersDTO(int limit) {
        return orderRepository.findLatestOrders().stream()
                .limit(limit)
                .map(order -> {
                    String customerName = order.getCustomer() != null ? 
                            order.getCustomer().getFullName() : order.getShippingFullName();
                    String customerPhone = order.getCustomer() != null ? 
                            order.getCustomer().getPhone() : order.getShippingPhone();
                    
                    return OrderDTO.builder()
                            .id(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                            .customerName(customerName)
                            .customerPhone(customerPhone)
                            .status(order.getStatus() != null ? order.getStatus().name() : null)
                            .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                            .totalAmount(order.getTotalAmount())
                            .shippingFullAddress(order.getShippingFullAddress())
                            .orderDate(order.getOrderDate())
                            .createdAt(order.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CustomerDTO> getRecentCustomersDTO(int limit) {
        return customerRepository.findAll().stream()
                .sorted((c1, c2) -> {
                    if (c1.getUser() == null || c1.getUser().getCreatedAt() == null) return 1;
                    if (c2.getUser() == null || c2.getUser().getCreatedAt() == null) return -1;
                    return c2.getUser().getCreatedAt().compareTo(c1.getUser().getCreatedAt());
                })
                .limit(limit)
                .map(customer -> {
                    return CustomerDTO.builder()
                            .id(customer.getId())
                            .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                            .fullName(customer.getFullName())
                            .phone(customer.getPhone())
                            .email(customer.getUser().getEmail())
                            .avatarUrl(customer.getUser() != null ? customer.getUser().getAvatarUrl() : null)
                            .totalOrders(customer.getTotalOrders())
                            .totalSpent(customer.getTotalSpent())
                            .createdAt(customer.getUser() != null ? customer.getUser().getCreatedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CategoryStatsDTO> getCategoryStats() {
        var categories = categoryRepository.findAll();
        long totalProducts = productRepository.count();
        
        return categories.stream()
                .map(category -> {
                    long productCount = productRepository.countByCategoryId(category.getId());
                    double percentage = totalProducts > 0 ? (double) productCount / totalProducts * 100 : 0;
                    
                    return CategoryStatsDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .slug(category.getSlug())
                            .productCount((int) productCount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal getRevenueOrZero(BigDecimal revenue) {
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
