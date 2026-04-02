package ntfur.com.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Shipping;
import ntfur.com.entity.Shipping.ShippingMethod;
import ntfur.com.entity.Shipping.ShippingStatus;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long>, JpaSpecificationExecutor<Shipping> {

    Optional<Shipping> findByOrderId(Long orderId);

    Optional<Shipping> findByTrackingNumber(String trackingNumber);

    List<Shipping> findByStatus(ShippingStatus status);

    List<Shipping> findByShippingMethod(ShippingMethod method);

    @Query("SELECT s FROM Shipping s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Shipping> findByStatusOrderByCreatedAtDesc(@Param("status") ShippingStatus status);

    @Query("SELECT s FROM Shipping s WHERE s.installationRequired = true AND s.installationCompletedAt IS NULL")
    List<Shipping> findPendingInstallations();

    @Query("SELECT s FROM Shipping s WHERE s.installationRequired = true AND s.status = 'DELIVERED'")
    List<Shipping> findDeliveredPendingInstallations();

    @Query("SELECT COUNT(s) FROM Shipping s WHERE s.status = :status")
    long countByStatus(@Param("status") ShippingStatus status);

    @Query("SELECT s FROM Shipping s WHERE s.pickupDate BETWEEN :startDate AND :endDate")
    List<Shipping> findByPickupDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Shipping s WHERE s.deliveryDate BETWEEN :startDate AND :endDate")
    List<Shipping> findByDeliveryDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Shipping s WHERE s.attemptCount >= :minAttempts")
    List<Shipping> findFailedDeliveries(@Param("minAttempts") int minAttempts);
}
