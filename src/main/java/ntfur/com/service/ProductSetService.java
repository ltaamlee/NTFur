package ntfur.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Product;
import ntfur.com.entity.ProductSet;
import ntfur.com.entity.dto.ProductSetDTO;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.ProductSetRepository;

@Service
@RequiredArgsConstructor
public class ProductSetService {

    private final ProductSetRepository productSetRepository;
    private final ProductRepository productRepository;

    public List<ProductSetDTO> getAllProductSets() {
        return productSetRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductSetDTO getProductSetById(Long id) {
        ProductSet productSet = productSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm với id: " + id));
        return toDTO(productSet);
    }

    public List<ProductSetDTO> getActiveProductSets() {
        return productSetRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductSetDTO createProductSet(ProductSetDTO dto) {
        ProductSet productSet = new ProductSet();
        productSet.setName(dto.getName());
        productSet.setDescription(dto.getDescription());
        productSet.setImageUrl(dto.getImageUrl());
        productSet.setActive(dto.isActive());

        ProductSet saved = productSetRepository.save(productSet);
        return toDTO(saved);
    }

    @Transactional
    public ProductSetDTO updateProductSet(Long id, ProductSetDTO dto) {
        ProductSet productSet = productSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm với id: " + id));

        if (dto.getName() != null) productSet.setName(dto.getName());
        if (dto.getDescription() != null) productSet.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) productSet.setImageUrl(dto.getImageUrl());
        productSet.setActive(dto.isActive());

        ProductSet updated = productSetRepository.save(productSet);
        return toDTO(updated);
    }

    @Transactional
    public ProductSetDTO addProductToSet(Long productSetId, Long productId) {
        ProductSet productSet = productSetRepository.findById(productSetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm với id: " + productSetId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));

        product.setProductSet(productSet);
        productRepository.save(product);

        productSet.getProducts().add(product);
        productSet.calculateTotalPrice();
        productSetRepository.save(productSet);

        return toDTO(productSet);
    }

    @Transactional
    public ProductSetDTO removeProductFromSet(Long productSetId, Long productId) {
        ProductSet productSet = productSetRepository.findById(productSetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm với id: " + productSetId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));

        product.setProductSet(null);
        productRepository.save(product);

        productSet.getProducts().remove(product);
        productSet.calculateTotalPrice();
        productSetRepository.save(productSet);

        return toDTO(productSet);
    }

    @Transactional
    public void deleteProductSet(Long id) {
        if (!productSetRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bộ sản phẩm với id: " + id);
        }
        productSetRepository.deleteById(id);
    }

    private ProductSetDTO toDTO(ProductSet productSet) {
        List<ProductDTO> productDTOs = productSet.getProducts().stream()
                .map(p -> ProductDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getPrice())
                        .costPrice(p.getCostPrice())
                        .stock(p.getStock())
                        .sku(p.getSku())
                        .status(p.getStatus() != null ? p.getStatus().name() : null)
                        .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                        .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                        .mainImage(p.getMainImage())
                        .featured(p.isFeatured())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ProductSetDTO.builder()
                .id(productSet.getId())
                .name(productSet.getName())
                .slug(productSet.getSlug())
                .description(productSet.getDescription())
                .imageUrl(productSet.getImageUrl())
                .totalPrice(productSet.getTotalPrice())
                .active(productSet.isActive())
                .products(productDTOs)
                .productCount(productSet.getProducts().size())
                .createdAt(productSet.getCreatedAt() != null ? productSet.getCreatedAt().toString() : null)
                .updatedAt(productSet.getUpdatedAt() != null ? productSet.getUpdatedAt().toString() : null)
                .build();
    }
}
