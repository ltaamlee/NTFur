package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.ImportReceipt;
import ntfur.com.entity.ImportReceipt.ImportStatus;
import ntfur.com.entity.ImportReceiptItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.Supplier;
import ntfur.com.entity.dto.ImportReceiptDTO;
import ntfur.com.repository.ImportReceiptItemRepository;
import ntfur.com.repository.ImportReceiptRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.SupplierProductRepository;
import ntfur.com.repository.SupplierRepository;

@Service
@RequiredArgsConstructor
public class ImportReceiptService {

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptItemRepository importReceiptItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SupplierProductRepository supplierProductRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<ImportReceiptDTO> getAllImportReceipts() {
        return importReceiptRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ImportReceiptDTO getImportReceiptById(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với id: " + id));
        return toDTO(receipt);
    }

    public List<ImportReceiptDTO> getImportReceiptsBySupplier(Long supplierId) {
        return importReceiptRepository.findBySupplierId(supplierId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ImportReceiptDTO> searchImportReceipts(String keyword) {
        return importReceiptRepository.searchByReceiptCode(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ImportReceiptDTO createImportReceipt(ImportReceiptDTO dto) {
        // ========== VALIDATION ==========
        // Kiểm tra nhà cung cấp bắt buộc
        if (dto.getSupplierId() == null) {
            throw new RuntimeException("Vui lòng chọn nhà cung cấp");
        }
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        // Kiểm tra danh sách sản phẩm không rỗng
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm nhập kho không được để trống");
        }

        // Kiểm tra từng sản phẩm
        for (ImportReceiptDTO.ImportReceiptItemDTO itemDTO : dto.getItems()) {
            // Số lượng phải > 0
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new RuntimeException("Số lượng sản phẩm phải lớn hơn 0");
            }
            // Đơn giá phải > 0
            BigDecimal unitPrice = itemDTO.getUnitPrice();
            if (unitPrice == null && itemDTO.getUnitPriceNumber() != null) {
                unitPrice = BigDecimal.valueOf(itemDTO.getUnitPriceNumber().doubleValue());
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Đơn giá sản phẩm phải lớn hơn 0");
            }
            // Kiểm tra sản phẩm tồn tại trong hệ thống
            if (itemDTO.getProductId() == null) {
                throw new RuntimeException("Mã sản phẩm không hợp lệ");
            }
            if (!productRepository.existsById(itemDTO.getProductId())) {
                throw new RuntimeException("Sản phẩm không tồn tại trong hệ thống");
            }
        }

        // ========== TẠO PHIẾU NHẬP ==========
        ImportReceipt receipt = new ImportReceipt();
        receipt.setSupplier(supplier);

        // Xử lý ngày nhập - hỗ trợ nhiều định dạng
        LocalDateTime importDateTime;
        if (dto.getImportDate() != null && !dto.getImportDate().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dto.getImportDate(), DATE_FORMATTER);
                importDateTime = date.atStartOfDay();
            } catch (DateTimeParseException e) {
                try {
                    importDateTime = LocalDateTime.parse(dto.getImportDate());
                } catch (DateTimeParseException e2) {
                    importDateTime = LocalDateTime.now();
                }
            }
        } else {
            importDateTime = LocalDateTime.now();
        }
        receipt.setImportDate(importDateTime);

        receipt.setInvoiceNumber(dto.getInvoiceNumber());
        receipt.setNotes(dto.getNotes());
        receipt.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO);
        // Theo đặc tả: Tạo phiếu nhập → PENDING (chờ duyệt)
        receipt.setStatus(ImportStatus.PENDING);

        ImportReceipt savedReceipt = importReceiptRepository.save(receipt);

        // ========== THÊM ITEMS VÀ CẬP NHẬT TỒN KHO ==========
        for (ImportReceiptDTO.ImportReceiptItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Kiểm tra sản phẩm thuộc nhà cung cấp
            boolean belongsToSupplier = supplierProductRepository.existsBySupplierIdAndProductId(dto.getSupplierId(), itemDTO.getProductId())
                    || (product.getSupplier() != null && product.getSupplier().getId().equals(dto.getSupplierId()));

            if (!belongsToSupplier) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không thuộc nhà cung cấp này");
            }

            ImportReceiptItem item = new ImportReceiptItem();
            item.setImportReceipt(savedReceipt);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setQuantity(itemDTO.getQuantity());

            BigDecimal unitPrice = itemDTO.getUnitPrice();
            if (unitPrice == null && itemDTO.getUnitPriceNumber() != null) {
                unitPrice = BigDecimal.valueOf(itemDTO.getUnitPriceNumber().doubleValue());
            }
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            importReceiptItemRepository.save(item);
            savedReceipt.getItems().add(item);

            // Theo đặc tả: Tồn kho chỉ được cập nhật khi phiếu được hoàn tất (bước 10)
            // Nên không cập nhật tồn kho ở đây
        }

        savedReceipt.calculateTotal();
        ImportReceipt finalReceipt = importReceiptRepository.save(savedReceipt);

        // Cập nhật thống kê nhà cung cấp
        updateSupplierStats(receipt.getSupplier().getId());

        return toDTO(finalReceipt);
    }

    @Transactional
    public ImportReceiptDTO updateImportReceipt(Long id, ImportReceiptDTO dto) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với id: " + id));

        if (dto.getNotes() != null) receipt.setNotes(dto.getNotes());
        if (dto.getDiscountAmount() != null) receipt.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getStatus() != null) receipt.setStatus(ImportStatus.valueOf(dto.getStatus()));

        ImportReceipt updated = importReceiptRepository.save(receipt);
        return toDTO(updated);
    }

    @Transactional
    public ImportReceiptDTO completeImportReceipt(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với id: " + id));

        if (receipt.getStatus() == ImportStatus.COMPLETED) {
            throw new RuntimeException("Phiếu nhập đã được hoàn tất trước đó");
        }
        if (receipt.getStatus() == ImportStatus.CANCELLED) {
            throw new RuntimeException("Không thể hoàn tất phiếu nhập đã bị hủy");
        }

        // Update product stock for each item
        for (ImportReceiptItem item : receipt.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        receipt.setStatus(ImportStatus.COMPLETED);
        ImportReceipt completed = importReceiptRepository.save(receipt);

        // Update supplier stats
        updateSupplierStats(receipt.getSupplier().getId());

        return toDTO(completed);
    }

    @Transactional
    public ImportReceiptDTO cancelImportReceipt(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với id: " + id));

        if (receipt.getStatus() == ImportStatus.CANCELLED) {
            throw new RuntimeException("Phiếu nhập đã bị hủy trước đó");
        }
        if (receipt.getStatus() == ImportStatus.COMPLETED) {
            // Reverse stock changes if previously completed
            for (ImportReceiptItem item : receipt.getItems()) {
                Product product = item.getProduct();
                product.setStock(Math.max(0, product.getStock() - item.getQuantity()));
                productRepository.save(product);
            }
        }

        receipt.setStatus(ImportStatus.CANCELLED);
        ImportReceipt cancelled = importReceiptRepository.save(receipt);

        return toDTO(cancelled);
    }

    @Transactional
    public void deleteImportReceipt(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với id: " + id));

        // Reverse stock changes only if receipt was completed
        if (receipt.getStatus() == ImportStatus.COMPLETED) {
            for (ImportReceiptItem item : receipt.getItems()) {
                Product product = item.getProduct();
                product.setStock(Math.max(0, product.getStock() - item.getQuantity()));
                productRepository.save(product);
            }
        }

        importReceiptRepository.delete(receipt);
    }

    private void updateSupplierStats(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        
        List<ImportReceipt> receipts = importReceiptRepository.findBySupplierId(supplierId);
        long orderCount = receipts.stream()
                .filter(r -> r.getStatus() == ImportStatus.COMPLETED)
                .count();
        double totalAmount = receipts.stream()
                .filter(r -> r.getStatus() == ImportStatus.COMPLETED)
                .mapToDouble(r -> r.getFinalAmount() != null ? r.getFinalAmount().doubleValue() : 0)
                .sum();
        
        supplier.setTotalOrders((int) orderCount);
        supplier.setTotalAmount(totalAmount);
        supplierRepository.save(supplier);
    }

    private ImportReceiptDTO toDTO(ImportReceipt receipt) {
        List<ImportReceiptDTO.ImportReceiptItemDTO> itemDTOs = receipt.getItems().stream()
                .map(item -> ImportReceiptDTO.ImportReceiptItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return ImportReceiptDTO.builder()
                .id(receipt.getId())
                .receiptCode(receipt.getReceiptCode())
                .supplierId(receipt.getSupplier() != null ? receipt.getSupplier().getId() : null)
                .supplierName(receipt.getSupplier() != null ? receipt.getSupplier().getName() : null)
                .importDate(receipt.getImportDate() != null ? receipt.getImportDate().toLocalDate().toString() : null)
                .invoiceNumber(receipt.getInvoiceNumber())
                .totalAmount(receipt.getTotalAmount())
                .discountAmount(receipt.getDiscountAmount())
                .finalAmount(receipt.getFinalAmount())
                .status(receipt.getStatus() != null ? receipt.getStatus().name() : null)
                .notes(receipt.getNotes())
                .items(itemDTOs)
                .itemCount(receipt.getItems().size())
                .createdAt(receipt.getCreatedAt() != null ? receipt.getCreatedAt().toString() : null)
                .updatedAt(receipt.getUpdatedAt() != null ? receipt.getUpdatedAt().toString() : null)
                .build();
    }
}
