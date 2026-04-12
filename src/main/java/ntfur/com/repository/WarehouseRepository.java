package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Warehouse;
import ntfur.com.entity.Warehouse.WarehouseStatus;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    List<Warehouse> findByStatus(WarehouseStatus status);

    List<Warehouse> findByStatusNot(WarehouseStatus status);

    @Query("SELECT w FROM Warehouse w WHERE w.status = 'ACTIVE' ORDER BY w.name")
    List<Warehouse> findAllActiveWarehouses();

    @Query("SELECT w FROM Warehouse w WHERE w.city = :city AND w.status = 'ACTIVE'")
    List<Warehouse> findByCityAndStatusActive(@Param("city") String city);

    @Query("SELECT w FROM Warehouse w LEFT JOIN FETCH w.warehouseProducts WHERE w.id = :id")
    Optional<Warehouse> findByIdWithProducts(@Param("id") Long id);

    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.status = 'ACTIVE'")
    long countActiveWarehouses();
}
