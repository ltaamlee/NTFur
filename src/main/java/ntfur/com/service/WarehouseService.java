package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ntfur.com.entity.ImportReceipt;
import ntfur.com.entity.Product;
import ntfur.com.entity.Warehouse;
import ntfur.com.entity.WarehouseProduct;
import ntfur.com.entity.WarehouseTransaction;
import ntfur.com.entity.WarehouseTransaction.ReferenceType;
import ntfur.com.entity.WarehouseTransaction.TransactionStatus;
import ntfur.com.entity.WarehouseTransaction.TransactionType;
import ntfur.com.entity.dto.WarehouseDTO;
import ntfur.com.repository.ImportReceiptRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.WarehouseProductRepository;
import ntfur.com.repository.WarehouseRepository;
import ntfur.com.repository.WarehouseTransactionRepository;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseProductRepository warehouseProductRepository;

    @Autowired
    private WarehouseTransactionRepository warehouseTransactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImportReceiptRepository importReceiptRepository;

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public List<Warehouse> getActiveWarehouses() {
        return warehouseRepository.findAllActiveWarehouses();
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    public Optional<Warehouse> getWarehouseByCode(String code) {
        return warehouseRepository.findByWarehouseCode(code);
    }

    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, Warehouse warehouseData) {
        Optional<Warehouse> warehouseOpt = warehouseRepository.findById(id);
        if (warehouseOpt.isPresent()) {
            Warehouse warehouse = warehouseOpt.get();
            warehouse.setName(warehouseData.getName());
            warehouse.setAddress(warehouseData.getAddress());
            warehouse.setCity(warehouseData.getCity());
            warehouse.setDistrict(warehouseData.getDistrict());
            warehouse.setWard(warehouseData.getWard());
            warehouse.setManagerName(warehouseData.getManagerName());
            warehouse.setPhone(warehouseData.getPhone());
            warehouse.setEmail(warehouseData.getEmail());
            warehouse.setStatus(warehouseData.getStatus());
            warehouse.setCapacity(warehouseData.getCapacity());
            warehouse.setDescription(warehouseData.getDescription());
            return warehouseRepository.save(warehouse);
        }
        return null;
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }

    @Transactional
    public WarehouseProduct importStock(Long warehouseId, Long productId, int quantity, BigDecimal unitPrice, String notes, String performedBy) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findByWarehouseAndProduct(warehouseId, productId)
                .orElseGet(() -> {
                    WarehouseProduct wp = new WarehouseProduct();
                    wp.setWarehouse(warehouse);
                    wp.setProduct(product);
                    wp.setQuantity(0);
                    wp.setImportPrice(unitPrice);
                    return wp;
                });

        warehouseProduct.importStock(quantity, unitPrice);
        warehouseProduct = warehouseProductRepository.save(warehouseProduct);

        WarehouseTransaction transaction = new WarehouseTransaction();
        transaction.setTransactionCode(generateTransactionCode(TransactionType.IMPORT));
        transaction.setTransactionType(TransactionType.IMPORT);
        transaction.setWarehouse(warehouse);
        transaction.setProduct(product);
        transaction.setProductName(product.getName());
        transaction.setProductSku(product.getSku());
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setReferenceType(ReferenceType.MANUAL);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setNotes(notes);
        transaction.setPerformedBy(performedBy);
        warehouseTransactionRepository.save(transaction);

        return warehouseProduct;
    }

    @Transactional
    public WarehouseProduct exportStock(Long warehouseId, Long productId, int quantity, String notes, String performedBy) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        WarehouseProduct warehouseProduct = warehouseProductRepository
                .findByWarehouseAndProduct(warehouseId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong kho"));

        warehouseProduct.exportStock(quantity);
        warehouseProduct = warehouseProductRepository.save(warehouseProduct);

        WarehouseTransaction transaction = new WarehouseTransaction();
        transaction.setTransactionCode(generateTransactionCode(TransactionType.EXPORT));
        transaction.setTransactionType(TransactionType.EXPORT);
        transaction.setWarehouse(warehouse);
        transaction.setProduct(product);
        transaction.setProductName(product.getName());
        transaction.setProductSku(product.getSku());
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(product.getPrice());
        transaction.setReferenceType(ReferenceType.MANUAL);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setNotes(notes);
        transaction.setPerformedBy(performedBy);
        warehouseTransactionRepository.save(transaction);

        return warehouseProduct;
    }

    @Transactional
    public void processImportReceipt(Long importReceiptId, Long warehouseId, String performedBy) {
        ImportReceipt receipt = importReceiptRepository.findById(importReceiptId)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu nhập không tồn tại"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));

        receipt.getItems().forEach(item -> {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            BigDecimal unitPrice = item.getUnitPrice();

            WarehouseProduct warehouseProduct = warehouseProductRepository
                    .findByWarehouseAndProduct(warehouseId, product.getId())
                    .orElseGet(() -> {
                        WarehouseProduct wp = new WarehouseProduct();
                        wp.setWarehouse(warehouse);
                        wp.setProduct(product);
                        wp.setQuantity(0);
                        return wp;
                    });

            warehouseProduct.importStock(quantity, unitPrice);
            warehouseProductRepository.save(warehouseProduct);

            WarehouseTransaction transaction = new WarehouseTransaction();
            transaction.setTransactionCode(generateTransactionCode(TransactionType.IMPORT));
            transaction.setTransactionType(TransactionType.IMPORT);
            transaction.setWarehouse(warehouse);
            transaction.setProduct(product);
            transaction.setProductName(item.getProductName());
            transaction.setProductSku(item.getSku());
            transaction.setQuantity(quantity);
            transaction.setUnitPrice(unitPrice);
            transaction.setReferenceType(ReferenceType.IMPORT_RECEIPT);
            transaction.setReferenceId(receipt.getId());
            transaction.setNotes("Nhập kho từ phiếu nhập: " + receipt.getReceiptCode());
            transaction.setPerformedBy(performedBy);
            warehouseTransactionRepository.save(transaction);
        });

        receipt.setStatus(ImportReceipt.ImportStatus.COMPLETED);
        importReceiptRepository.save(receipt);
    }

    @Transactional
    public void processOrderExport(Long orderId, Long warehouseId, String performedBy) {
        // Process when order is confirmed/shipped
        // This will be called from OrderService
    }

    public List<WarehouseProduct> getWarehouseProducts(Long warehouseId) {
        return warehouseProductRepository.findByWarehouseId(warehouseId);
    }

    public List<WarehouseTransaction> getWarehouseTransactions(Long warehouseId) {
        return warehouseTransactionRepository.findByWarehouseIdOrderByCreatedAtDesc(warehouseId);
    }

    public List<WarehouseTransaction> getProductTransactions(Long productId) {
        return warehouseTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Integer getTotalStockByProductId(Long productId) {
        Integer total = warehouseProductRepository.getTotalStockByProductId(productId);
        return total != null ? total : 0;
    }

    public Integer getTotalStockByWarehouseId(Long warehouseId) {
        Integer total = warehouseProductRepository.getTotalStockByWarehouseId(warehouseId);
        return total != null ? total : 0;
    }

    public boolean checkStockAvailable(Long productId, int quantity) {
        Integer totalStock = getTotalStockByProductId(productId);
        return totalStock >= quantity;
    }

    public List<WarehouseProduct> getProductInStock(Long productId) {
        return warehouseProductRepository.findProductInStock(productId);
    }

    public WarehouseDTO.WarehouseStockDTO getProductStockDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        List<WarehouseProduct> warehouseProducts = warehouseProductRepository.findByProductId(productId);

        int totalStock = 0;
        int reservedStock = 0;

        for (WarehouseProduct wp : warehouseProducts) {
            totalStock += wp.getQuantity() != null ? wp.getQuantity() : 0;
            reservedStock += wp.getQuantity() != null ? wp.getQuantity() : 0;
        }

        WarehouseDTO.WarehouseStockDTO stockDTO = new WarehouseDTO.WarehouseStockDTO();
        stockDTO.setProductId(productId);
        stockDTO.setProductName(product.getName());
        stockDTO.setProductSku(product.getSku());
        stockDTO.setTotalStock(totalStock);
        stockDTO.setReservedStock(reservedStock);
        stockDTO.setAvailableStock(totalStock - reservedStock);

        List<WarehouseDTO.WarehouseStockDetail> details = warehouseProducts.stream()
                .map(wp -> {
                    WarehouseDTO.WarehouseStockDetail detail = new WarehouseDTO.WarehouseStockDetail();
                    detail.setWarehouseId(wp.getWarehouse().getId());
                    detail.setWarehouseName(wp.getWarehouse().getName());
                    detail.setQuantity(wp.getQuantity());
                    detail.setAvailableQuantity(wp.getAvailableQuantity());
                    return detail;
                })
                .collect(Collectors.toList());

        stockDTO.setWarehouseDetails(details);
        return stockDTO;
    }

    public List<WarehouseTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return warehouseTransactionRepository.findByDateRange(startDate, endDate);
    }

    public WarehouseDTO.WarehouseReportDTO getWarehouseReport(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));

        List<WarehouseProduct> products = warehouseProductRepository.findByWarehouseId(warehouseId);
        List<WarehouseTransaction> transactions = warehouseTransactionRepository.findByWarehouseId(warehouseId);

        int totalStock = 0;
        int totalReserved = 0;
        int importCount = 0;
        int exportCount = 0;
        BigDecimal totalImportValue = BigDecimal.ZERO;
        BigDecimal totalExportValue = BigDecimal.ZERO;

        for (WarehouseProduct wp : products) {
            totalStock += wp.getQuantity() != null ? wp.getQuantity() : 0;
            totalReserved += wp.getQuantity() != null ? wp.getQuantity() : 0;
        }

        for (WarehouseTransaction t : transactions) {
            if (t.getStatus() == TransactionStatus.COMPLETED) {
                if (t.getTransactionType() == TransactionType.IMPORT) {
                    importCount++;
                    if (t.getUnitPrice() != null) {
                        totalImportValue = totalImportValue.add(t.getUnitPrice());
                    }
                } else if (t.getTransactionType() == TransactionType.EXPORT) {
                    exportCount++;
                    if (t.getUnitPrice() != null) {
                        totalExportValue = totalExportValue.add(t.getUnitPrice());
                    }
                }
            }
        }

        WarehouseDTO.WarehouseReportDTO report = new WarehouseDTO.WarehouseReportDTO();
        report.setWarehouseId(warehouseId);
        report.setWarehouseName(warehouse.getName());
        report.setTotalProducts(products.size());
        report.setTotalStock(totalStock);
        report.setTotalReserved(totalReserved);
        report.setTotalAvailable(totalStock - totalReserved);
        report.setImportCount(importCount);
        report.setExportCount(exportCount);
        report.setTotalImportValue(totalImportValue);
        report.setTotalExportValue(totalExportValue);

        return report;
    }

    private String generateTransactionCode(TransactionType type) {
        String prefix = type.name().substring(0, 3).toUpperCase();
        return prefix + "-" + System.currentTimeMillis();
    }
}
