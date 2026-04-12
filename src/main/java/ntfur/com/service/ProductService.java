package ntfur.com.service;

import java.math.BigDecimal;
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
import ntfur.com.entity.ProductVariant;
import ntfur.com.entity.Supplier;
import ntfur.com.entity.Warehouse;
import ntfur.com.entity.dto.ProductVariantDTO;
import ntfur.com.entity.dto.product.CreateProductRequest;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.entity.dto.product.ProductImageDTO;
import ntfur.com.entity.dto.product.ProductImageRequest;
import ntfur.com.entity.dto.product.UpdateProductRequest;
import ntfur.com.repository.CategoryRepository;
import ntfur.com.repository.ProductImageRepository;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.ProductSetRepository;
import ntfur.com.repository.ProductVariantRepository;
import ntfur.com.repository.SupplierRepository;
import ntfur.com.repository.WarehouseRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSetRepository productSetRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseService warehouseService;
    
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

        if (request.getStock() != null) {
            product.setStock(request.getStock());
            // Tự động cập nhật trạng thái dựa trên tồn kho khi tạo mới
            if (request.getStock() <= 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
        }
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

    /**
     * Giảm tồn kho - ưu tiên lấy từ Warehouse trước, fallback về Product.stock
     */
    @Transactional
    public boolean reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        // Ưu tiên lấy từ Warehouse nếu có
        List<Warehouse> activeWarehouses = warehouseRepository.findAllActiveWarehouses();
        if (!activeWarehouses.isEmpty()) {
            Warehouse warehouse = activeWarehouses.get(0);
            try {
                warehouseService.exportStock(warehouse.getId(), productId, quantity, 
                        "Giảm tồn kho sản phẩm", "System");
                return true;
            } catch (Exception e) {
                // Fallback về Product.stock nếu Warehouse lỗi
            }
        }
        
        // Fallback: Giảm trực tiếp trên Product
        if (product.getStock() < quantity) {
            return false;
        }
        
        product.setStock(product.getStock() - quantity);
        
        // Tự động cập nhật trạng thái nếu hết hàng
        if (product.getStock() <= 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        
        productRepository.save(product);
        return true;
    }

    /**
     * Khôi phục tồn kho - ưu tiên thêm vào Warehouse trước, fallback về Product.stock
     */
    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        // Ưu tiên thêm vào Warehouse nếu có
        List<Warehouse> activeWarehouses = warehouseRepository.findAllActiveWarehouses();
        if (!activeWarehouses.isEmpty()) {
            Warehouse warehouse = activeWarehouses.get(0);
            try {
                warehouseService.importStock(warehouse.getId(), productId, quantity, 
                        product.getCostPrice() != null ? product.getCostPrice() : product.getPrice(),
                        "Khôi phục tồn kho sản phẩm", "System");
                return;
            } catch (Exception e) {
                // Fallback về Product.stock nếu Warehouse lỗi
            }
        }
        
        // Fallback: Tăng trực tiếp trên Product
        int newStock = product.getStock() + quantity;
        product.setStock(newStock);
        
        // Tự động cập nhật trạng thái nếu có hàng trở lại
        if (newStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
    }

    /**
     * Đồng bộ trạng thái sản phẩm dựa trên tồn kho thực tế (từ Warehouse hoặc Product)
     */
    @Transactional
    public void syncProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + productId));
        
        // Lấy tồn kho thực tế từ Warehouse hoặc fallback về Product.stock
        int actualStock = 0;
        try {
            actualStock = warehouseService.getTotalStockByProductId(productId);
        } catch (Exception e) {
            actualStock = product.getStock();
        }
        
        if (actualStock <= 0 && product.getStatus() != ProductStatus.DISCONTINUED) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (actualStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
    }

    /**
     * Đồng bộ trạng thái cho TẤT CẢ sản phẩm
     */
    @Transactional
    public int syncAllProductStatus() {
        List<Product> products = productRepository.findAll();
        int count = 0;
        
        for (Product product : products) {
            int actualStock = product.getStock();
            
            // Thử lấy từ Warehouse
            try {
                int warehouseStock = warehouseService.getTotalStockByProductId(product.getId());
                if (warehouseStock > 0) {
                    actualStock = warehouseStock;
                }
            } catch (Exception e) {
                // Fallback
            }
            
            // Cập nhật stock từ Warehouse vào Product (nếu Warehouse có dữ liệu)
            try {
                int warehouseStock = warehouseService.getTotalStockByProductId(product.getId());
                if (warehouseStock > 0 || product.getStock() != warehouseStock) {
                    product.setStock(warehouseStock > 0 ? warehouseStock : product.getStock());
                }
            } catch (Exception e) {
                // Giữ nguyên stock
            }
            
            // Cập nhật status dựa trên stock thực tế
            if (actualStock <= 0 && product.getStatus() != ProductStatus.DISCONTINUED) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
                count++;
            } else if (actualStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
                count++;
            }
            
            productRepository.save(product);
        }
        
        return count;
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

        // Lấy biến thể của sản phẩm
        List<ProductVariantDTO> variants = productVariantRepository
                .findByProductIdAndActiveTrue(product.getId())
                .stream()
                .map(ProductVariantDTO::fromEntity)
                .collect(Collectors.toList());

        int totalStock = product.getStock();
        
        // Lấy tồn kho thực tế từ Warehouse (nếu có)
        try {
            int warehouseStock = warehouseService.getTotalStockByProductId(product.getId());
            if (warehouseStock > 0) {
                totalStock = warehouseStock;
            }
        } catch (Exception e) {
            // Fallback về product.getStock()
        }
        
        // Cộng thêm từ variants nếu có
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                if (v.isActive()) {
                    totalStock += v.getStock();
                }
            }
        }

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
                .variants(variants)
                .totalStock(totalStock)
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
