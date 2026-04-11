package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.service.ProductService;

/**
 * EmployeeProductController - Controller xử lý các API liên quan đến sản phẩm cho nhân viên
 * Cung cấp endpoint để employee xem danh sách sản phẩm
 */
@RestController
@RequestMapping("/api/employee/products")
@RequiredArgsConstructor
public class EmployeeProductController {

    private final ProductService productService;

    /**
     * Lấy danh sách tất cả sản phẩm cho nhân viên
     * @return Danh sách sản phẩm
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<?>>> getAllProducts() {
        List<?> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}