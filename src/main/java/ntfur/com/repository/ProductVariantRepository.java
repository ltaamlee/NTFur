package ntfur.com.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

    List<ProductVariant> findByProductIdAndAttributeType(Long productId, ProductVariant.AttributeType attributeType);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.active = true ORDER BY v.attributeType, v.attributeValue")
    List<ProductVariant> findActiveVariantsByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(v.stock) FROM ProductVariant v WHERE v.product.id = :productId AND v.active = true")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    boolean existsBySku(String sku);

    boolean existsByProductIdAndAttributeTypeAndAttributeValue(Long productId, ProductVariant.AttributeType attributeType, String attributeValue);
}
