package ntfur.com.controller.admin;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.SupplierDTO;
import ntfur.com.service.SupplierService;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {
	
    private final SupplierService supplierService;
    
    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà cung cấp thành công", suppliers));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplierById(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà cung cấp thành công", supplier));
    }

    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getActiveSuppliers() {
        List<SupplierDTO> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà cung cấp đang hoạt động thành công", suppliers));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> searchSuppliers(@RequestParam String keyword) {
        List<SupplierDTO> suppliers = supplierService.searchSuppliers(keyword);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm nhà cung cấp thành công", suppliers));
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierDTO>> createSupplier(@RequestBody SupplierDTO dto) {
        SupplierDTO supplier = supplierService.createSupplier(dto);
        return ResponseEntity.ok(ApiResponse.success("Thêm nhà cung cấp thành công", supplier));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO dto) {
        SupplierDTO supplier = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà cung cấp thành công", supplier));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhà cung cấp thành công", null));
    }

}
