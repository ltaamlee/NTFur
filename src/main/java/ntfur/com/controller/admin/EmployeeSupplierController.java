package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.service.SupplierService;

/**
 * EmployeeSupplierController - Controller xử lý các API liên quan đến nhà cung cấp cho nhân viên
 * Cung cấp endpoint để employee xem danh sách nhà cung cấp
 */
@RestController
@RequestMapping("/api/employee/suppliers")
@RequiredArgsConstructor
public class EmployeeSupplierController {

    private final SupplierService supplierService;

    /**
     * Lấy danh sách tất cả nhà cung cấp cho nhân viên
     * @return Danh sách nhà cung cấp
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<?>>> getAllSuppliers() {
        List<?> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }
}