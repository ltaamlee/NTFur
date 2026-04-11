package ntfur.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Product;
import ntfur.com.entity.ProductVariant;
import ntfur.com.entity.dto.ProductVariantDTO;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.ProductVariantRepository;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        return variantRepository.findActiveVariantsByProductId(productId).stream()
                .map(ProductVariantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Map<String, List<ProductVariantDTO>> getVariantsGroupedByType(Long productId) {
        List<ProductVariantDTO> variants = getVariantsByProductId(productId);
        return variants.stream()
                .collect(Collectors.groupingBy(ProductVariantDTO::getAttributeType));
    }

    public ProductVariantDTO getVariantById(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + variantId));
        return ProductVariantDTO.fromEntity(variant);
    }

    @Transactional
    public ProductVariantDTO createVariant(Long productId, ProductVariantDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));

        // Kiểm tra trùng lặp
        if (dto.getAttributeType() != null && dto.getAttributeValue() != null) {
            ProductVariant.AttributeType attrType = ProductVariant.AttributeType.valueOf(dto.getAttributeType());
            if (variantRepository.existsByProductIdAndAttributeTypeAndAttributeValue(productId, attrType, dto.getAttributeValue())) {
                throw new RuntimeException("Biến thể với " + attrType.name() + ": " + dto.getAttributeValue() + " đã tồn tại");
            }
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setVariantName(dto.getVariantName());
        variant.setAttributeType(dto.getAttributeType() != null ? ProductVariant.AttributeType.valueOf(dto.getAttributeType()) : null);
        variant.setAttributeValue(dto.getAttributeValue());
        variant.setSku(dto.getSku());
        variant.setPriceAdjustment(dto.getPriceAdjustment() != null ? dto.getPriceAdjustment() : BigDecimal.ZERO);
        variant.setStock(dto.getStock());
        variant.setActive(dto.isActive());
        variant.setImageUrl(dto.getImageUrl());

        ProductVariant saved = variantRepository.save(variant);
        return ProductVariantDTO.fromEntity(saved);
    }

    @Transactional
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO dto) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + variantId));

        if (dto.getVariantName() != null) variant.setVariantName(dto.getVariantName());
        if (dto.getAttributeType() != null) variant.setAttributeType(ProductVariant.AttributeType.valueOf(dto.getAttributeType()));
        if (dto.getAttributeValue() != null) variant.setAttributeValue(dto.getAttributeValue());
        if (dto.getSku() != null) variant.setSku(dto.getSku());
        if (dto.getPriceAdjustment() != null) variant.setPriceAdjustment(dto.getPriceAdjustment());
        variant.setStock(dto.getStock());
        variant.setActive(dto.isActive());
        if (dto.getImageUrl() != null) variant.setImageUrl(dto.getImageUrl());

        ProductVariant saved = variantRepository.save(variant);
        return ProductVariantDTO.fromEntity(saved);
    }

    @Transactional
    public void updateVariantStock(Long variantId, int quantityChange) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + variantId));

        int newStock = variant.getStock() + quantityChange;
        if (newStock < 0) {
            throw new RuntimeException("Số lượng tồn kho không đủ. Hiện tại: " + variant.getStock());
        }
        variant.setStock(newStock);
        variantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new RuntimeException("Không tìm thấy biến thể với id: " + variantId);
        }
        variantRepository.deleteById(variantId);
    }

    public int getTotalStockByProductId(Long productId) {
        Integer totalStock = variantRepository.getTotalStockByProductId(productId);
        return totalStock != null ? totalStock : 0;
    }
}
