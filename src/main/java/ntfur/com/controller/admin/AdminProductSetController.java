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
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.ProductSetDTO;
import ntfur.com.service.ProductSetService;

@RestController
@RequestMapping("/api/admin/product-sets")
@RequiredArgsConstructor
public class AdminProductSetController {

    private final ProductSetService productSetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSetDTO>>> getAllProductSets() {
        List<ProductSetDTO> productSets = productSetService.getAllProductSets();
        return ResponseEntity.ok(ApiResponse.success(productSets));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductSetDTO>>> getActiveProductSets() {
        List<ProductSetDTO> productSets = productSetService.getActiveProductSets();
        return ResponseEntity.ok(ApiResponse.success(productSets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSetDTO>> getProductSetById(@PathVariable Long id) {
        try {
            ProductSetDTO productSet = productSetService.getProductSetById(id);
            return ResponseEntity.ok(ApiResponse.success(productSet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSetDTO>> createProductSet(@RequestBody ProductSetDTO request) {
        try {
            ProductSetDTO productSet = productSetService.createProductSet(request);
            return ResponseEntity.ok(ApiResponse.success("Tạo bộ sản phẩm thành công", productSet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSetDTO>> updateProductSet(@PathVariable Long id, @RequestBody ProductSetDTO request) {
        try {
            ProductSetDTO productSet = productSetService.updateProductSet(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật bộ sản phẩm thành công", productSet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<ProductSetDTO>> addProductToSet(@PathVariable Long id, @PathVariable Long productId) {
        try {
            ProductSetDTO productSet = productSetService.addProductToSet(id, productId);
            return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm vào bộ thành công", productSet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<ProductSetDTO>> removeProductFromSet(@PathVariable Long id, @PathVariable Long productId) {
        try {
            ProductSetDTO productSet = productSetService.removeProductFromSet(id, productId);
            return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi bộ thành công", productSet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductSet(@PathVariable Long id) {
        try {
            productSetService.deleteProductSet(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa bộ sản phẩm thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
