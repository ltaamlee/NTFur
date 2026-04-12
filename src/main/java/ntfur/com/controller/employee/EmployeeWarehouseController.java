package ntfur.com.controller.employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.User;
import ntfur.com.entity.Warehouse;
import ntfur.com.entity.WarehouseProduct;
import ntfur.com.entity.WarehouseTransaction;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.WarehouseDTO;
import ntfur.com.service.WarehouseService;

@RestController
@RequestMapping("/api/employee/warehouse")
@RequiredArgsConstructor
public class EmployeeWarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Lấy danh sách tất cả kho
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        List<WarehouseDTO> dtos = warehouses.stream()
                .map(this::toWarehouseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy danh sách kho đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getActiveWarehouses() {
        List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
        List<WarehouseDTO> dtos = warehouses.stream()
                .map(this::toWarehouseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy thông tin kho theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouseById(@PathVariable Long id) {
        return warehouseService.getWarehouseById(id)
                .map(warehouse -> ResponseEntity.ok(ApiResponse.success(toWarehouseDTO(warehouse))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy kho")));
    }

    /**
     * Lấy sản phẩm trong kho
     */
    @GetMapping("/{warehouseId}/products")
    public ResponseEntity<ApiResponse<List<WarehouseDTO.WarehouseProductDTO>>> getWarehouseProducts(
            @PathVariable Long warehouseId) {
        List<WarehouseProduct> products = warehouseService.getWarehouseProducts(warehouseId);
        List<WarehouseDTO.WarehouseProductDTO> dtos = products.stream()
                .map(this::toWarehouseProductDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy sản phẩm trong kho theo mã sản phẩm
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<WarehouseDTO.WarehouseStockDTO>> getProductStock(@PathVariable Long productId) {
        try {
            WarehouseDTO.WarehouseStockDTO stock = warehouseService.getProductStockDetails(productId);
            return ResponseEntity.ok(ApiResponse.success(stock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử giao dịch kho
     */
    @GetMapping("/{warehouseId}/transactions")
    public ResponseEntity<ApiResponse<List<WarehouseDTO.WarehouseTransactionDTO>>> getWarehouseTransactions(
            @PathVariable Long warehouseId) {
        List<WarehouseTransaction> transactions = warehouseService.getWarehouseTransactions(warehouseId);
        List<WarehouseDTO.WarehouseTransactionDTO> dtos = transactions.stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy lịch sử giao dịch theo sản phẩm
     */
    @GetMapping("/products/{productId}/transactions")
    public ResponseEntity<ApiResponse<List<WarehouseDTO.WarehouseTransactionDTO>>> getProductTransactions(
            @PathVariable Long productId) {
        List<WarehouseTransaction> transactions = warehouseService.getProductTransactions(productId);
        List<WarehouseDTO.WarehouseTransactionDTO> dtos = transactions.stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy tồn kho theo ngày
     */
    @GetMapping("/transactions/by-date")
    public ResponseEntity<ApiResponse<List<WarehouseDTO.WarehouseTransactionDTO>>> getTransactionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<WarehouseTransaction> transactions = warehouseService.getTransactionsByDateRange(start, end);
            List<WarehouseDTO.WarehouseTransactionDTO> dtos = transactions.stream()
                    .map(this::toTransactionDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Định dạng ngày không hợp lệ (yyyy-MM-ddTHH:mm:ss)"));
        }
    }

    /**
     * Nhập kho thủ công
     */
    @PostMapping("/{warehouseId}/import")
    public ResponseEntity<ApiResponse<WarehouseDTO.WarehouseProductDTO>> importStock(
            @AuthenticationPrincipal User user,
            @PathVariable Long warehouseId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.containsKey("unitPrice") 
                    ? new BigDecimal(request.get("unitPrice").toString()) 
                    : BigDecimal.ZERO;
            String notes = request.containsKey("notes") ? request.get("notes").toString() : null;

            WarehouseProduct warehouseProduct = warehouseService.importStock(
                    warehouseId, productId, quantity, unitPrice, notes, user.getUsername());

            return ResponseEntity.ok(ApiResponse.success("Nhập kho thành công", toWarehouseProductDTO(warehouseProduct)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xuất kho thủ công
     */
    @PostMapping("/{warehouseId}/export")
    public ResponseEntity<ApiResponse<WarehouseDTO.WarehouseProductDTO>> exportStock(
            @AuthenticationPrincipal User user,
            @PathVariable Long warehouseId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            String notes = request.containsKey("notes") ? request.get("notes").toString() : null;

            WarehouseProduct warehouseProduct = warehouseService.exportStock(
                    warehouseId, productId, quantity, notes, user.getUsername());

            return ResponseEntity.ok(ApiResponse.success("Xuất kho thành công", toWarehouseProductDTO(warehouseProduct)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xử lý phiếu nhập kho
     */
    @PostMapping("/import-receipt/{receiptId}/process")
    public ResponseEntity<ApiResponse<String>> processImportReceipt(
            @AuthenticationPrincipal User user,
            @PathVariable Long receiptId,
            @RequestBody Map<String, Object> request) {
        try {
            Long warehouseId = Long.valueOf(request.get("warehouseId").toString());
            warehouseService.processImportReceipt(receiptId, warehouseId, user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Xử lý phiếu nhập kho thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy báo cáo kho
     */
    @GetMapping("/{warehouseId}/report")
    public ResponseEntity<ApiResponse<WarehouseDTO.WarehouseReportDTO>> getWarehouseReport(@PathVariable Long warehouseId) {
        try {
            WarehouseDTO.WarehouseReportDTO report = warehouseService.getWarehouseReport(warehouseId);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Kiểm tra tồn kho
     */
    @GetMapping("/check-stock")
    public ResponseEntity<ApiResponse<Boolean>> checkStock(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        boolean available = warehouseService.checkStockAvailable(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    // ========== HELPER METHODS ==========

    private WarehouseDTO toWarehouseDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setWarehouseCode(warehouse.getWarehouseCode());
        dto.setName(warehouse.getName());
        dto.setAddress(warehouse.getAddress());
        dto.setCity(warehouse.getCity());
        dto.setDistrict(warehouse.getDistrict());
        dto.setWard(warehouse.getWard());
        dto.setManagerName(warehouse.getManagerName());
        dto.setPhone(warehouse.getPhone());
        dto.setEmail(warehouse.getEmail());
        dto.setStatus(warehouse.getStatus() != null ? warehouse.getStatus().name() : null);
        dto.setCapacity(warehouse.getCapacity());
        dto.setDescription(warehouse.getDescription());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        return dto;
    }

    private WarehouseDTO.WarehouseProductDTO toWarehouseProductDTO(WarehouseProduct wp) {
        WarehouseDTO.WarehouseProductDTO dto = new WarehouseDTO.WarehouseProductDTO();
        dto.setId(wp.getId());
        dto.setWarehouseId(wp.getWarehouse() != null ? wp.getWarehouse().getId() : null);
        dto.setWarehouseName(wp.getWarehouse() != null ? wp.getWarehouse().getName() : null);
        dto.setProductId(wp.getProduct() != null ? wp.getProduct().getId() : null);
        dto.setProductName(wp.getProduct() != null ? wp.getProduct().getName() : null);
        dto.setProductSku(wp.getProduct() != null ? wp.getProduct().getSku() : null);
        dto.setQuantity(wp.getQuantity());
        dto.setAvailableQuantity(wp.getAvailableQuantity());
        dto.setImportPrice(wp.getImportPrice());
        return dto;
    }

    private WarehouseDTO.WarehouseTransactionDTO toTransactionDTO(WarehouseTransaction t) {
        WarehouseDTO.WarehouseTransactionDTO dto = new WarehouseDTO.WarehouseTransactionDTO();
        dto.setId(t.getId());
        dto.setTransactionCode(t.getTransactionCode());
        dto.setTransactionType(t.getTransactionType() != null ? t.getTransactionType().name() : null);
        dto.setWarehouseId(t.getWarehouse() != null ? t.getWarehouse().getId() : null);
        dto.setWarehouseName(t.getWarehouse() != null ? t.getWarehouse().getName() : null);
        dto.setProductId(t.getProduct() != null ? t.getProduct().getId() : null);
        dto.setProductName(t.getProductName());
        dto.setProductSku(t.getProductSku());
        dto.setQuantity(t.getQuantity());
        dto.setUnitPrice(t.getUnitPrice());
        dto.setReferenceType(t.getReferenceType() != null ? t.getReferenceType().name() : null);
        dto.setReferenceId(t.getReferenceId());
        dto.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        dto.setNotes(t.getNotes());
        dto.setPerformedBy(t.getPerformedBy());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}
