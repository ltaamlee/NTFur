package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.WarehouseProduct;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, Long> {

    Optional<WarehouseProduct> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<WarehouseProduct> findByWarehouseId(Long warehouseId);

    List<WarehouseProduct> findByProductId(Long productId);

    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.warehouse.id = :warehouseId AND wp.product.id = :productId")
    Optional<WarehouseProduct> findByWarehouseAndProduct(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.quantity > 0 AND wp.warehouse.status = 'ACTIVE'")
    List<WarehouseProduct> findAllInStock();

    @Query("SELECT wp FROM WarehouseProduct wp WHERE wp.product.id = :productId AND wp.quantity > 0")
    List<WarehouseProduct> findProductInStock(@Param("productId") Long productId);

    @Query("SELECT SUM(wp.quantity) FROM WarehouseProduct wp WHERE wp.product.id = :productId")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(wp.quantity) FROM WarehouseProduct wp WHERE wp.warehouse.id = :warehouseId")
    Integer getTotalStockByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Modifying
    @Query("UPDATE WarehouseProduct wp SET wp.quantity = wp.quantity + :quantity WHERE wp.warehouse.id = :warehouseId AND wp.product.id = :productId")
    int increaseStock(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE WarehouseProduct wp SET wp.quantity = wp.quantity - :quantity WHERE wp.warehouse.id = :warehouseId AND wp.product.id = :productId AND wp.quantity >= :quantity")
    int decreaseStock(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId, @Param("quantity") int quantity);

    boolean existsByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
