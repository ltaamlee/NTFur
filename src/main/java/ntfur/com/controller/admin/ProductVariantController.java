package ntfur.com.controller.admin;

import java.util.List;
import java.util.Map;

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
import ntfur.com.entity.dto.ProductVariantDTO;
import ntfur.com.service.ProductVariantService;

@RestController
@RequestMapping("/api/admin/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductVariantDTO>>> getVariantsByProduct(@PathVariable Long productId) {
        List<ProductVariantDTO> variants = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/product/{productId}/grouped")
    public ResponseEntity<ApiResponse<Map<String, List<ProductVariantDTO>>>> getVariantsGrouped(@PathVariable Long productId) {
        Map<String, List<ProductVariantDTO>> variants = variantService.getVariantsGroupedByType(productId);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> getVariantById(@PathVariable Long variantId) {
        try {
            ProductVariantDTO variant = variantService.getVariantById(variantId);
            return ResponseEntity.ok(ApiResponse.success(variant));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> createVariant(@PathVariable Long productId, @RequestBody ProductVariantDTO request) {
        try {
            ProductVariantDTO variant = variantService.createVariant(productId, request);
            return ResponseEntity.ok(ApiResponse.success("Tạo biến thể thành công", variant));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateVariant(@PathVariable Long variantId, @RequestBody ProductVariantDTO request) {
        try {
            ProductVariantDTO variant = variantService.updateVariant(variantId, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật biến thể thành công", variant));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long variantId) {
        try {
            variantService.deleteVariant(variantId);
            return ResponseEntity.ok(ApiResponse.success("Xóa biến thể thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}/stock")
    public ResponseEntity<ApiResponse<Integer>> getTotalStock(@PathVariable Long productId) {
        int totalStock = variantService.getTotalStockByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(totalStock));
    }
}
