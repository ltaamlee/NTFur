package ntfur.com.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Order;
import ntfur.com.entity.Order.OrderStatus;
import ntfur.com.entity.Order.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT o FROM Order o WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.shippingFullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR o.shippingPhone LIKE CONCAT('%', :keyword, '%')")
    List<Order> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findLatestOrders();

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findTopOrders(int limit);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = :paymentStatus")
    BigDecimal getTotalRevenueByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID'")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT CAST(o.createdAt AS DATE), SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :startDate GROUP BY CAST(o.createdAt AS DATE) ORDER BY CAST(o.createdAt AS DATE)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT MONTH(o.createdAt), YEAR(o.createdAt), SUM(o.totalAmount) FROM Order o WHERE YEAR(o.createdAt) = :year GROUP BY MONTH(o.createdAt), YEAR(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT YEAR(o.createdAt), SUM(o.totalAmount) FROM Order o GROUP BY YEAR(o.createdAt) ORDER BY YEAR(o.createdAt)")
    List<Object[]> getYearlyRevenue();

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.paymentStatus = 'PENDING' AND o.paymentDeadline < :now")
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' AND o.paymentStatus = 'PENDING' AND o.paymentDeadline < :now")
    long countExpiredPendingOrders(@Param("now") LocalDateTime now);
}
