package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO {

    private Long id;
    private String warehouseCode;
    private String name;
    private String address;
    private String city;
    private String district;
    private String ward;
    private String managerName;
    private String phone;
    private String email;
    private String status;
    private Integer capacity;
    private Integer currentStock;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseProductDTO {
        private Long id;
        private Long warehouseId;
        private String warehouseName;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private BigDecimal importPrice;
        private LocalDateTime lastImportDate;
        private LocalDateTime lastExportDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseTransactionDTO {
        private Long id;
        private String transactionCode;
        private String transactionType;
        private Long warehouseId;
        private String warehouseName;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String referenceType;
        private Long referenceId;
        private String referenceCode;
        private String status;
        private String notes;
        private String performedBy;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseStockDTO {
        private Long productId;
        private String productName;
        private String productSku;
        private Integer totalStock;
        private Integer reservedStock;
        private Integer availableStock;
        private List<WarehouseStockDetail> warehouseDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseStockDetail {
        private Long warehouseId;
        private String warehouseName;
        private Integer quantity;
        private Integer availableQuantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseReportDTO {
        private Long warehouseId;
        private String warehouseName;
        private Integer totalProducts;
        private Integer totalStock;
        private Integer totalReserved;
        private Integer totalAvailable;
        private Integer importCount;
        private Integer exportCount;
        private BigDecimal totalImportValue;
        private BigDecimal totalExportValue;
    }
}
