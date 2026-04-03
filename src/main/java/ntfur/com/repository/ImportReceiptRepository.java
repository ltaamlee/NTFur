package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.ImportReceipt;
import ntfur.com.entity.Supplier;

@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {

    List<ImportReceipt> findBySupplierId(Long supplierId);

    List<ImportReceipt> findBySupplier(Supplier supplier);

    @Query("SELECT ir FROM ImportReceipt ir WHERE LOWER(ir.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ImportReceipt> searchByReceiptCode(String keyword);

    Optional<ImportReceipt> findByReceiptCode(String receiptCode);
}
