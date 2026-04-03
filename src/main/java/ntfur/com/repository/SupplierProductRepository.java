package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.SupplierProduct;

@Repository
public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    List<SupplierProduct> findBySupplierId(Long supplierId);

    List<SupplierProduct> findBySupplierIdAndActiveTrue(Long supplierId);

    List<SupplierProduct> findByProductId(Long productId);

    Optional<SupplierProduct> findBySupplierIdAndProductId(Long supplierId, Long productId);

    Optional<SupplierProduct> findBySku(String sku);

    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);

    boolean existsBySku(String sku);

    @Query("SELECT sp FROM SupplierProduct sp WHERE sp.supplier.id = :supplierId AND sp.product.id = :productId AND sp.active = true")
    Optional<SupplierProduct> findActiveBySupplierAndProduct(@Param("supplierId") Long supplierId, @Param("productId") Long productId);

    @Query("SELECT COUNT(sp) FROM SupplierProduct sp WHERE sp.supplier.id = :supplierId AND sp.active = true")
    long countActiveBySupplierId(@Param("supplierId") Long supplierId);
}
