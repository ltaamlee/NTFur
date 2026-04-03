package ntfur.com.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Product;
import ntfur.com.entity.Supplier;
import ntfur.com.entity.Supplier.SupplierStatus;
import ntfur.com.entity.dto.SupplierDTO;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.repository.ProductRepository;
import ntfur.com.repository.SupplierRepository;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với id: " + id));
        return toDTO(supplier);
    }

    public SupplierDTO getSupplierBySlug(String slug) {
        Supplier supplier = supplierRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với slug: " + slug));
        return toDTO(supplier);
    }

    public List<SupplierDTO> getActiveSuppliers() {
        return supplierRepository.findByStatus(SupplierStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SupplierDTO> searchSuppliers(String keyword) {
        return supplierRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierDTO createSupplier(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setDescription(dto.getDescription());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setCity(dto.getCity());
        supplier.setCountry(dto.getCountry() != null ? dto.getCountry() : "Vietnam");
        supplier.setTaxCode(dto.getTaxCode());
        supplier.setBankAccount(dto.getBankAccount());
        supplier.setBankName(dto.getBankName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setContactEmail(dto.getContactEmail());
        supplier.setWebsite(dto.getWebsite());
        supplier.setLogoUrl(dto.getLogoUrl());
        supplier.setNotes(dto.getNotes());
        supplier.setStatus(dto.getStatus() != null ? SupplierStatus.valueOf(dto.getStatus()) : SupplierStatus.ACTIVE);

        Supplier saved = supplierRepository.save(supplier);
        
        // Xử lý sản phẩm
        addProductsToSupplier(saved, dto.getProductNames(), dto.getProductIds());
        
        return toDTO(supplierRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với id: " + id));

        if (dto.getName() != null) supplier.setName(dto.getName());
        if (dto.getDescription() != null) supplier.setDescription(dto.getDescription());
        if (dto.getEmail() != null) supplier.setEmail(dto.getEmail());
        if (dto.getPhone() != null) supplier.setPhone(dto.getPhone());
        if (dto.getAddress() != null) supplier.setAddress(dto.getAddress());
        if (dto.getCity() != null) supplier.setCity(dto.getCity());
        if (dto.getCountry() != null) supplier.setCountry(dto.getCountry());
        if (dto.getTaxCode() != null) supplier.setTaxCode(dto.getTaxCode());
        if (dto.getBankAccount() != null) supplier.setBankAccount(dto.getBankAccount());
        if (dto.getBankName() != null) supplier.setBankName(dto.getBankName());
        if (dto.getContactPerson() != null) supplier.setContactPerson(dto.getContactPerson());
        if (dto.getContactPhone() != null) supplier.setContactPhone(dto.getContactPhone());
        if (dto.getContactEmail() != null) supplier.setContactEmail(dto.getContactEmail());
        if (dto.getWebsite() != null) supplier.setWebsite(dto.getWebsite());
        if (dto.getLogoUrl() != null) supplier.setLogoUrl(dto.getLogoUrl());
        if (dto.getNotes() != null) supplier.setNotes(dto.getNotes());
        if (dto.getStatus() != null) supplier.setStatus(SupplierStatus.valueOf(dto.getStatus()));

        Supplier updated = supplierRepository.save(supplier);
        
        // Xử lý sản phẩm nếu có thay đổi
        if (dto.getProductNames() != null || dto.getProductIds() != null) {
            addProductsToSupplier(updated, dto.getProductNames(), dto.getProductIds());
        }
        
        return toDTO(supplierRepository.findById(updated.getId()).orElse(updated));
    }

    private void addProductsToSupplier(Supplier supplier, List<String> productNames, List<Long> productIds) {
        List<Product> productsToAdd = new ArrayList<>();
        
        // Gán sản phẩm có sẵn
        if (productIds != null && !productIds.isEmpty()) {
            for (Long productId : productIds) {
                if (productId != null && !isTempId(productId)) {
                    productRepository.findById(productId).ifPresent(product -> {
                        product.setSupplier(supplier);
                        productsToAdd.add(product);
                    });
                }
            }
        }
        
        // Tạo sản phẩm mới từ tên
        if (productNames != null && !productNames.isEmpty()) {
            for (String productName : productNames) {
                if (productName != null && !productName.trim().isEmpty()) {
                    Product newProduct = new Product();
                    newProduct.setName(productName.trim());
                    newProduct.setSupplier(supplier);
                    newProduct.setStatus(Product.ProductStatus.ACTIVE);
                    newProduct.setStock(0);
                    newProduct.setPrice(BigDecimal.valueOf(0.0));
                    productsToAdd.add(newProduct);
                }
            }
        }
        
        if (!productsToAdd.isEmpty()) {
            productRepository.saveAll(productsToAdd);
        }
    }

    private boolean isTempId(Long id) {
        return id != null && id < 0;
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy nhà cung cấp với id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    public long countSuppliers() {
        return supplierRepository.count();
    }

    public long countByStatus(String status) {
        return supplierRepository.countByStatus(SupplierStatus.valueOf(status));
    }

    private SupplierDTO toDTO(Supplier supplier) {
        List<ProductDTO> productDTOs = supplier.getProducts().stream()
                .map(p -> ProductDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .sku(p.getSku())
                        .status(p.getStatus() != null ? p.getStatus().name() : null)
                        .mainImage(p.getMainImage())
                        .featured(p.isFeatured())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SupplierDTO.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .slug(supplier.getSlug())
                .code(supplier.getCode())
                .description(supplier.getDescription())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .taxCode(supplier.getTaxCode())
                .bankAccount(supplier.getBankAccount())
                .bankName(supplier.getBankName())
                .contactPerson(supplier.getContactPerson())
                .contactPhone(supplier.getContactPhone())
                .contactEmail(supplier.getContactEmail())
                .website(supplier.getWebsite())
                .logoUrl(supplier.getLogoUrl())
                .rating(supplier.getRating())
                .totalOrders(supplier.getTotalOrders())
                .totalAmount(supplier.getTotalAmount())
                .status(supplier.getStatus() != null ? supplier.getStatus().name() : null)
                .notes(supplier.getNotes())
                .products(productDTOs)
                .productCount(supplier.getProducts().size())
                .createdAt(supplier.getCreatedAt() != null ? supplier.getCreatedAt().toString() : null)
                .updatedAt(supplier.getUpdatedAt() != null ? supplier.getUpdatedAt().toString() : null)
                .build();
    }
}