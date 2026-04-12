package ntfur.com.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.WarehouseTransaction;
import ntfur.com.entity.WarehouseTransaction.TransactionStatus;
import ntfur.com.entity.WarehouseTransaction.TransactionType;

@Repository
public interface WarehouseTransactionRepository extends JpaRepository<WarehouseTransaction, Long> {

    Optional<WarehouseTransaction> findByTransactionCode(String transactionCode);

    List<WarehouseTransaction> findByWarehouseId(Long warehouseId);

    List<WarehouseTransaction> findByProductId(Long productId);

    List<WarehouseTransaction> findByTransactionType(TransactionType transactionType);

    List<WarehouseTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.warehouse.id = :warehouseId ORDER BY wt.createdAt DESC")
    List<WarehouseTransaction> findByWarehouseIdOrderByCreatedAtDesc(@Param("warehouseId") Long warehouseId);

    @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.product.id = :productId ORDER BY wt.createdAt DESC")
    List<WarehouseTransaction> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.createdAt BETWEEN :startDate AND :endDate ORDER BY wt.createdAt DESC")
    List<WarehouseTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.referenceType = :refType AND wt.referenceId = :refId")
    List<WarehouseTransaction> findByReference(@Param("refType") WarehouseTransaction.ReferenceType refType, @Param("refId") Long refId);

    @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.warehouse.id = :warehouseId AND wt.transactionType = :type AND wt.createdAt BETWEEN :startDate AND :endDate")
    List<WarehouseTransaction> findByWarehouseAndTypeAndDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(wt.quantity) FROM WarehouseTransaction wt WHERE wt.product.id = :productId AND wt.transactionType = 'IMPORT' AND wt.status = 'COMPLETED'")
    Integer getTotalImportQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(wt.quantity) FROM WarehouseTransaction wt WHERE wt.product.id = :productId AND wt.transactionType = 'EXPORT' AND wt.status = 'COMPLETED'")
    Integer getTotalExportQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(wt) FROM WarehouseTransaction wt WHERE wt.warehouse.id = :warehouseId")
    long countByWarehouseId(@Param("warehouseId") Long warehouseId);
}
