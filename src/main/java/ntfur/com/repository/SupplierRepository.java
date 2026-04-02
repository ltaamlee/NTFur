package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Supplier;
import ntfur.com.entity.Supplier.SupplierStatus;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySlug(String slug);

    Optional<Supplier> findByCode(String code);

    boolean existsByEmail(String email);

    boolean existsByCode(String code);

    List<Supplier> findByStatus(SupplierStatus status);

    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> searchByKeyword(String keyword);

    List<Supplier> findByCity(String city);

    long countByStatus(SupplierStatus status);
}
