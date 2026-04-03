package ntfur.com.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Category;
import ntfur.com.entity.Product;
import ntfur.com.entity.Product.ProductStatus;
import ntfur.com.entity.ProductImage;
import ntfur.com.entity.ProductSet;
import ntfur.com.entity.Supplier;
import ntfur.com.entity.dto.product.CreateProductRequest;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.entity.dto.product.ProductImageDTO;
import ntfur.com.entity.dto.product.ProductImageRequest;
import ntfur.com.entity.dto.product.UpdateProductRequest;
import ntfur.com.repository.CategoryRepository;
import ntfur.com.repository.ProductImageRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.ProductSetRepository;
import ntfur.com.repository.SupplierRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSetRepository productSetRepository;
    
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        return toDTO(product);
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> getProductsByCategoryIncludingSubcategories(Long categoryId) {
        // Get all category IDs including subcategories
        List<Long> categoryIds = categoryRepository.findAllChildIds(categoryId);
        categoryIds.add(categoryId);
        return productRepository.findByCategoryIdIn(categoryIds).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(BigDecimal.valueOf(request.getPrice()));
        
        if (request.getCostPrice() != null) {
            product.setCostPrice(BigDecimal.valueOf(request.getCostPrice()));
        }
        
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setSku(request.getSku());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setMaterial(request.getMaterial());
        product.setColor(request.getColor());
        product.setWarrantyMonths(request.getWarrantyMonths());
        product.setFeatured(request.isFeatured());
        
        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus()));
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
            product.setSupplier(supplier);
        }
        
        // Xử lý ProductSet - ưu tiên tạo mới nếu có newProductSetName
        if (request.getNewProductSetName() != null && !request.getNewProductSetName().trim().isEmpty()) {
            ProductSet newSet = new ProductSet();
            newSet.setName(request.getNewProductSetName().trim());
            newSet.setActive(true);
            ProductSet savedSet = productSetRepository.save(newSet);
            product.setProductSet(savedSet);
        } else if (request.getProductSetId() != null) {
            ProductSet productSet = productSetRepository.findById(request.getProductSetId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm"));
            product.setProductSet(productSet);
        }
        
        Product savedProduct = productRepository.save(product);
        
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveProductImages(savedProduct, request.getImages());
        } else if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProduct(savedProduct);
                image.setImageUrl(request.getImageUrls().get(i));
                image.setDisplayOrder(i);
                image.setPrimary(i == 0);
                productImageRepository.save(image);
            }
        }
        
        return getProductById(savedProduct.getId());
    }

    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(BigDecimal.valueOf(request.getPrice()));
        }
        if (request.getCostPrice() != null) {
            product.setCostPrice(BigDecimal.valueOf(request.getCostPrice()));
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
            // Tự động cập nhật trạng thái dựa trên tồn kho
            if (request.getStock() <= 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        if (request.getMaterial() != null) {
            product.setMaterial(request.getMaterial());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getWarrantyMonths() != null) {
            product.setWarrantyMonths(request.getWarrantyMonths());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus()));
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
            product.setSupplier(supplier);
        }
        // Xử lý ProductSet - ưu tiên tạo mới nếu có newProductSetName
        if (request.getNewProductSetName() != null && !request.getNewProductSetName().trim().isEmpty()) {
            ProductSet newSet = new ProductSet();
            newSet.setName(request.getNewProductSetName().trim());
            newSet.setActive(true);
            ProductSet savedSet = productSetRepository.save(newSet);
            product.setProductSet(savedSet);
        } else if (request.getProductSetId() != null) {
            ProductSet productSet = productSetRepository.findById(request.getProductSetId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sản phẩm"));
            product.setProductSet(productSet);
        }

        Product updatedProduct = productRepository.save(product);
        
        if (request.getImages() != null) {
            productImageRepository.deleteByProductId(id);
            saveProductImages(updatedProduct, request.getImages());
        } else if (request.getImageUrls() != null) {
            productImageRepository.deleteByProductId(id);
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProduct(updatedProduct);
                image.setImageUrl(request.getImageUrls().get(i));
                image.setDisplayOrder(i);
                image.setPrimary(i == 0);
                productImageRepository.save(image);
            }
        }
        
        return getProductById(updatedProduct.getId());
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với id: " + id);
        }
        productImageRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    public long countProducts() {
        return productRepository.count();
    }

    @Transactional
    public boolean reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        if (product.getStock() < quantity) {
            return false; // Không đủ tồn kho
        }
        
        product.setStock(product.getStock() - quantity);
        
        // Tự động cập nhật trạng thái nếu hết hàng
        if (product.getStock() <= 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        
        productRepository.save(product);
        return true;
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        int newStock = product.getStock() + quantity;
        product.setStock(newStock);
        
        // Tự động cập nhật trạng thái nếu có hàng trở lại
        if (newStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
    }

    @Transactional
    public void syncProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        if (product.getStock() <= 0 && product.getStatus() != ProductStatus.DISCONTINUED) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStock() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
    }

    private void saveProductImages(Product product, List<ProductImageRequest> imageRequests) {
        for (int i = 0; i < imageRequests.size(); i++) {
            ProductImageRequest imgReq = imageRequests.get(i);
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imgReq.getImageUrl());
            image.setPublicId(imgReq.getPublicId());
            image.setAltText(imgReq.getAltText());
            image.setDisplayOrder(imgReq.getDisplayOrder() > 0 ? imgReq.getDisplayOrder() : i);
            image.setPrimary(imgReq.isPrimary() || i == 0);
            productImageRepository.save(image);
        }
    }

    private ProductDTO toDTO(Product product) {
        List<ProductImageDTO> images = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId()) 
                .stream()
                .map(img -> ProductImageDTO.builder()
                        .id(img.getId())
                        .productId(img.getProduct() != null ? img.getProduct().getId() : null)
                        .imageUrl(img.getImageUrl())
                        .publicId(img.getPublicId())
                        .altText(img.getAltText())
                        .displayOrder(img.getDisplayOrder())
                        .isPrimary(img.isPrimary())
                        .createdAt(img.getCreatedAt() != null ? img.getCreatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .stock(product.getStock())
                .sku(product.getSku())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .productSetId(product.getProductSet() != null ? product.getProductSet().getId() : null)
                .productSetName(product.getProductSet() != null ? product.getProductSet().getName() : null)
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .material(product.getMaterial())
                .color(product.getColor())
                .warrantyMonths(product.getWarrantyMonths())
                .featured(product.isFeatured())
                .mainImage(product.getMainImage())
                .images(images)
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
