package ntfur.com.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.ImportReceiptItem;

@Repository
public interface ImportReceiptItemRepository extends JpaRepository<ImportReceiptItem, Long> {

    List<ImportReceiptItem> findByImportReceiptId(Long importReceiptId);
}
