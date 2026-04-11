package ntfur.com.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportReceiptDTO {

    private Long id;
    private String receiptCode;
    private Long supplierId;
    private String supplierName;
    private String importDate;
    private String invoiceNumber;  // Số hóa đơn / chứng từ từ nhà cung cấp
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
    private String notes;
    private List<ImportReceiptItemDTO> items;
    private Integer itemCount;
    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportReceiptItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private Number unitPriceNumber; // For handling numeric values from frontend
        private BigDecimal totalPrice;
    }
}
