package ntfur.com.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.ImportReceiptDTO;
import ntfur.com.service.ImportReceiptService;

@RestController
@RequestMapping("/api/employee/import-receipts")
@RequiredArgsConstructor
public class EmployeeImportReceiptController {

    private final ImportReceiptService importReceiptService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImportReceiptDTO>>> getAllImportReceipts() {
        List<ImportReceiptDTO> receipts = importReceiptService.getAllImportReceipts();
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> getImportReceiptById(@PathVariable Long id) {
        try {
            ImportReceiptDTO receipt = importReceiptService.getImportReceiptById(id);
            return ResponseEntity.ok(ApiResponse.success(receipt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<ImportReceiptDTO>>> getImportReceiptsBySupplier(@PathVariable Long supplierId) {
        List<ImportReceiptDTO> receipts = importReceiptService.getImportReceiptsBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ImportReceiptDTO>>> searchImportReceipts(@RequestParam String keyword) {
        List<ImportReceiptDTO> receipts = importReceiptService.searchImportReceipts(keyword);
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> createImportReceipt(@RequestBody ImportReceiptDTO request) {
        try {
            ImportReceiptDTO receipt = importReceiptService.createImportReceipt(request);
            return ResponseEntity.ok(ApiResponse.success("Tạo phiếu nhập hàng thành công", receipt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> updateImportReceipt(@PathVariable Long id, @RequestBody ImportReceiptDTO request) {
        try {
            ImportReceiptDTO receipt = importReceiptService.updateImportReceipt(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu nhập hàng thành công", receipt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> completeImportReceipt(@PathVariable Long id) {
        try {
            ImportReceiptDTO receipt = importReceiptService.completeImportReceipt(id);
            return ResponseEntity.ok(ApiResponse.success("Hoàn thành phiếu nhập hàng", receipt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> cancelImportReceipt(@PathVariable Long id) {
        try {
            ImportReceiptDTO receipt = importReceiptService.cancelImportReceipt(id);
            return ResponseEntity.ok(ApiResponse.success("Hủy phiếu nhập hàng", receipt));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImportReceipt(@PathVariable Long id) {
        try {
            importReceiptService.deleteImportReceipt(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa phiếu nhập hàng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
