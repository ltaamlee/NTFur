package ntfur.com.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.User;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.CartDTO;
import ntfur.com.entity.dto.CartItemDTO;
import ntfur.com.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Lấy thông tin giỏ hàng hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal User user) {
        CartDTO cart = cartService.getCartDTO(user);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = request.containsKey("quantity") 
                    ? Integer.parseInt(request.get("quantity").toString()) : 1;
            
            CartItemDTO item = cartService.addToCart(user, productId, quantity);
            return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> request) {
        try {
            int quantity = Integer.parseInt(request.get("quantity").toString());
            CartItemDTO item = cartService.updateCartItem(user, productId, quantity);
            if (item == null) {
                return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", null));
            }
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        try {
            cartService.removeFromCart(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng", null));
    }
}