package ntfur.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Cart;
import ntfur.com.entity.CartItem;
import ntfur.com.entity.Product;
import ntfur.com.entity.User;
import ntfur.com.entity.dto.CartDTO;
import ntfur.com.entity.dto.CartItemDTO;
import ntfur.com.repository.CartItemRepository;
import ntfur.com.repository.CartRepository;
import ntfur.com.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy hoặc tạo giỏ hàng cho user
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Transactional
    public CartItemDTO addToCart(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm không khả dụng");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ. Còn " + product.getStock() + " sản phẩm");
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho. Còn " + product.getStock() + " sản phẩm");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            existingItem = new CartItem();
            existingItem.setCart(cart);
            existingItem.setProduct(product);
            existingItem.setQuantity(quantity);
            existingItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(existingItem);
            cart.getItems().add(existingItem);
        }

        recalculateCart(cart);
        return toItemDTO(existingItem);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Transactional
    public CartItemDTO updateCartItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        if (quantity <= 0) {
            removeFromCart(user, productId);
            return null;
        }

        Product product = item.getProduct();
        if (product.getStock() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ. Còn " + product.getStock() + " sản phẩm");
        }

        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        cartItemRepository.save(item);
        recalculateCart(cart);

        return toItemDTO(item);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public void removeFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        recalculateCart(cart);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setItemCount(0);
        cartRepository.save(cart);
    }

    /**
     * Lấy thông tin giỏ hàng
     */
    public CartDTO getCartDTO(User user) {
        Cart cart = getOrCreateCart(user);
        return toDTO(cart);
    }

    /**
     * Tính lại tổng tiền giỏ hàng
     */
    private void recalculateCart(Cart cart) {
        cart.recalculateTotal();
        cartRepository.save(cart);
    }

    /**
     * Chuyển đổi Cart entity sang DTO
     */
    public CartDTO toDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        BigDecimal total = itemDTOs.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemDTOs)
                .itemCount(cart.getItems().size())
                .totalAmount(total)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi CartItem entity sang DTO
     */
    private CartItemDTO toItemDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getMainImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .createdAt(item.getCreatedAt())
                .build();
    }
}