package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Order;
import ntfur.com.entity.Product;
import ntfur.com.entity.dto.DashboardDTO;
import ntfur.com.entity.dto.OrderDTO;
import ntfur.com.entity.dto.RevenueDataPoint;
import ntfur.com.entity.dto.RevenueStatsDTO;
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
        long confirmedOrders = orderRepository.countByStatus(Order.OrderStatus.CONFIRMED);
        long processingOrders = orderRepository.countByStatus(Order.OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long returnedOrders = orderRepository.countByStatus(Order.OrderStatus.RETURNED);
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
                .confirmedOrders(confirmedOrders)
                .processingOrders(processingOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .returnedOrders(returnedOrders)
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

    /**
     * Lấy thống kê doanh thu theo kỳ (ngày, tuần, tháng, năm hoặc tùy chỉnh)
     * @param period Loại kỳ: DAY, WEEK, MONTH, YEAR, CUSTOM
     * @param startDate Ngày bắt đầu (cho CUSTOM)
     * @param endDate Ngày kết thúc (cho CUSTOM)
     * @return RevenueStatsDTO chứa thông tin thống kê chi tiết
     */
    public RevenueStatsDTO getRevenueStats(String period, LocalDateTime startDate, LocalDateTime endDate) {
        // Xác định khoảng thời gian dựa trên loại kỳ
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart;
        LocalDateTime rangeEnd = now;
        List<RevenueDataPoint> chartData;
        double growthRate = 0.0;

        switch (period.toUpperCase()) {
            case "DAY":
                // Hôm nay
                rangeStart = now.with(LocalTime.MIN);
                chartData = getRevenueChartForDay();
                // So sánh với hôm qua
                LocalDateTime yesterdayStart = now.minusDays(1).with(LocalTime.MIN);
                LocalDateTime yesterdayEnd = now.minusDays(1).with(LocalTime.MAX);
                BigDecimal todayRevenue = getRevenueInRange(rangeStart, now);
                BigDecimal yesterdayRevenue = getRevenueInRange(yesterdayStart, yesterdayEnd);
                growthRate = calculateGrowthRate(todayRevenue, yesterdayRevenue);
                break;

            case "WEEK":
                // 7 ngày qua
                rangeStart = now.minusDays(7).with(LocalTime.MIN);
                chartData = getRevenueChartForWeek();
                // So sánh với tuần trước
                BigDecimal thisWeekRevenue = getRevenueInRange(rangeStart, now);
                BigDecimal lastWeekRevenue = getRevenueInRange(now.minusDays(14).with(LocalTime.MIN), now.minusDays(7).with(LocalTime.MAX));
                growthRate = calculateGrowthRate(thisWeekRevenue, lastWeekRevenue);
                break;

            case "MONTH":
                // 30 ngày qua
                rangeStart = now.minusDays(30).with(LocalTime.MIN);
                chartData = getRevenueChartForMonth();
                // So sánh với tháng trước
                BigDecimal thisMonthRevenue = getRevenueInRange(rangeStart, now);
                BigDecimal lastMonthRevenue = getRevenueInRange(now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN),
                        now.minusMonths(1).withDayOfMonth(now.getDayOfMonth()).with(LocalTime.MAX));
                growthRate = calculateGrowthRate(thisMonthRevenue, lastMonthRevenue);
                break;

            case "YEAR":
                // 12 tháng qua
                rangeStart = now.minusYears(1).withDayOfYear(1).with(LocalTime.MIN);
                chartData = getRevenueChartForYear();
                // So sánh với năm trước
                BigDecimal thisYearRevenue = getRevenueInRange(rangeStart, now);
                BigDecimal lastYearRevenue = getRevenueInRange(now.minusYears(1).withDayOfYear(1).with(LocalTime.MIN),
                        now.minusYears(1).withDayOfYear(now.getDayOfYear()).with(LocalTime.MAX));
                growthRate = calculateGrowthRate(thisYearRevenue, lastYearRevenue);
                break;

            case "CUSTOM":
            default:
                // Tùy chỉnh - dùng startDate và endDate
                if (startDate == null || endDate == null) {
                    throw new RuntimeException("Vui lòng chọn ngày bắt đầu và ngày kết thúc");
                }
                rangeStart = startDate.with(LocalTime.MIN);
                rangeEnd = endDate.with(LocalTime.MAX);
                chartData = getRevenueChartForCustom(rangeStart, rangeEnd);

                // Tính độ dài kỳ để so sánh với kỳ trước
                long daysDiff = java.time.Duration.between(rangeStart, rangeEnd).toDays();
                LocalDateTime prevStart = rangeStart.minusDays(daysDiff);
                LocalDateTime prevEnd = rangeStart.minusDays(1).with(LocalTime.MAX);
                BigDecimal thisPeriodRevenue = getRevenueInRange(rangeStart, rangeEnd);
                BigDecimal lastPeriodRevenue = getRevenueInRange(prevStart, prevEnd);
                growthRate = calculateGrowthRate(thisPeriodRevenue, lastPeriodRevenue);
                break;
        }

        // Lấy danh sách đơn hàng trong kỳ để tính toán
        List<Order> ordersInRange = orderRepository.findByCreatedAtBetween(rangeStart, rangeEnd);

        // Tính tổng doanh thu (chỉ đơn đã thanh toán)
        BigDecimal totalRevenue = ordersInRange.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Đếm đơn hàng
        long totalOrders = ordersInRange.size();
        long paidOrders = ordersInRange.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .count();
        long unpaidOrders = totalOrders - paidOrders;

        // Tính giá trị trung bình mỗi đơn
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(paidOrders > 0 ? paidOrders : 1), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RevenueStatsDTO.builder()
                .period(period.toUpperCase())
                .startDate(rangeStart)
                .endDate(rangeEnd)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .paidOrders(paidOrders)
                .unpaidOrders(unpaidOrders)
                .growthRate(Math.round(growthRate * 10.0) / 10.0)
                .chartData(chartData)
                .build();
    }

    /**
     * Lấy biểu đồ doanh thu theo ngày (cho period=DAY)
     */
    private List<RevenueDataPoint> getRevenueChartForDay() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(now.with(LocalTime.MIN), now);

        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        // Giả sử lấy 24 giờ trong ngày
        for (int hour = 0; hour <= now.getHour(); hour++) {
            LocalDateTime hourStart = now.withHour(hour).withMinute(0).withSecond(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);

            BigDecimal hourRevenue = todayOrders.stream()
                    .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                    .filter(o -> o.getCreatedAt() != null)
                    .filter(o -> {
                        LocalDateTime created = o.getCreatedAt();
                        return !created.isBefore(hourStart) && created.isBefore(hourEnd);
                    })
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dataPoints.add(RevenueDataPoint.builder()
                    .date(String.format("%02d:00", hour))
                    .revenue(hourRevenue)
                    .orderCount((int) todayOrders.stream()
                            .filter(o -> o.getCreatedAt() != null)
                            .filter(o -> {
                                LocalDateTime created = o.getCreatedAt();
                                return !created.isBefore(hourStart) && created.isBefore(hourEnd);
                            })
                            .count())
                    .build());
        }
        return dataPoints;
    }

    /**
     * Lấy biểu đồ doanh thu theo tuần (7 ngày)
     */
    private List<RevenueDataPoint> getRevenueChartForWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7).with(LocalTime.MIN);

        List<Object[]> results = orderRepository.getDailyRevenue(weekStart);
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

    /**
     * Lấy biểu đồ doanh thu theo tháng (30 ngày)
     */
    private List<RevenueDataPoint> getRevenueChartForMonth() {
        return getRevenueChart(30);
    }

    /**
     * Lấy biểu đồ doanh thu theo năm (12 tháng)
     */
    private List<RevenueDataPoint> getRevenueChartForYear() {
        List<Object[]> results = orderRepository.getMonthlyRevenue(LocalDateTime.now().getYear());
        List<RevenueDataPoint> dataPoints = new ArrayList<>();

        for (Object[] result : results) {
            Integer month = result[0] != null ? (Integer) result[0] : 0;
            BigDecimal revenue = result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO;
            dataPoints.add(RevenueDataPoint.builder()
                    .date(String.format("Tháng %02d", month))
                    .revenue(revenue)
                    .orderCount(0)
                    .build());
        }
        return dataPoints;
    }

    /**
     * Lấy biểu đồ doanh thu cho kỳ tùy chỉnh
     */
    private List<RevenueDataPoint> getRevenueChartForCustom(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);

        // Nhóm theo ngày
        return orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> RevenueDataPoint.builder()
                        .date(entry.getKey().toString())
                        .revenue(entry.getValue().stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .orderCount(entry.getValue().size())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy doanh thu trong một khoảng thời gian
     */
    private BigDecimal getRevenueInRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        return orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính tỷ lệ tăng trưởng (%)
     */
    private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
