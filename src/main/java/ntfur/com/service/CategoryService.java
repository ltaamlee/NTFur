package ntfur.com.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Category;
import ntfur.com.entity.Category.CategoryStatus;
import ntfur.com.entity.dto.category.CategoryDTO;
import ntfur.com.entity.dto.category.CreateCategoryRequest;
import ntfur.com.entity.dto.category.UpdateCategoryRequest;
import ntfur.com.repository.CategoryRepository;
import ntfur.com.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getAllCategoriesWithHierarchy() {
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryDTO> allCategoryDTOs = allCategories.stream()
                .map(this::toDTOWithChildren)
                .collect(Collectors.toList());
        
        // Build hierarchy
        return buildCategoryHierarchy(allCategoryDTOs);
    }
    
    public List<CategoryDTO> getActiveCategoriesWithHierarchy() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndStatusOrderByDisplayOrder(CategoryStatus.ACTIVE);
        return rootCategories.stream()
                .map(this::toDTOWithChildrenRecursive)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByStatusOrderByDisplayOrder(CategoryStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrder().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderByDisplayOrder(parentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
        return toDTO(category);
    }

    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với slug: " + slug));
        return toDTOWithChildrenRecursive(category);
    }

    public List<CategoryDTO> searchCategories(String keyword) {
        return categoryRepository.searchByKeyword(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        
        if (request.getStatus() != null) {
            category.setStatus(CategoryStatus.valueOf(request.getStatus()));
        } else {
            category.setStatus(CategoryStatus.ACTIVE);
        }
        
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        
        // Set parent category if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + request.getParentId()));
            category.setParent(parent);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return toDTO(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));

        if (request.getName() != null) {
            if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
                throw new RuntimeException("Tên danh mục đã tồn tại");
            }
            category.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null) {
            category.setStatus(CategoryStatus.valueOf(request.getStatus()));
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        // Update parent category if provided
        if (request.getParentId() != null) {
            // Prevent setting itself as parent or circular reference
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Danh mục không thể là cha của chính nó");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục với id: " + id);
        }
        
        // Check if category has products
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa danh mục vì có " + productCount + " sản phẩm đang thuộc danh mục này");
        }
        
        // Move children to root or delete them
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrder(id);
        for (Category child : children) {
            child.setParent(null);
            categoryRepository.save(child);
        }
        
        categoryRepository.deleteById(id);
    }

    public long countCategories() {
        return categoryRepository.count();
    }
    
    private List<CategoryDTO> buildCategoryHierarchy(List<CategoryDTO> categories) {
        List<CategoryDTO> roots = new ArrayList<>();
        for (CategoryDTO dto : categories) {
            if (dto.getParentId() == null) {
                roots.add(dto);
            }
        }
        
        for (CategoryDTO root : roots) {
            assignChildren(root, categories);
        }
        
        return roots;
    }
    
    private void assignChildren(CategoryDTO parent, List<CategoryDTO> allCategories) {
        List<CategoryDTO> children = new ArrayList<>();
        for (CategoryDTO dto : allCategories) {
            if (dto.getParentId() != null && dto.getParentId().equals(parent.getId())) {
                children.add(dto);
                assignChildren(dto, allCategories);
            }
        }
        parent.setChildren(children);
    }

    private CategoryDTO toDTO(Category category) {
        int productCount = (int) productRepository.countByCategoryId(category.getId());
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .status(category.getStatus() != null ? category.getStatus().name() : null)
                .displayOrder(category.getDisplayOrder())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    
    private CategoryDTO toDTOWithChildren(Category category) {
        int productCount = (int) productRepository.countByCategoryId(category.getId());
        
        List<CategoryDTO> childrenDTOs = new ArrayList<>();
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                childrenDTOs.add(toDTOWithChildren(child));
            }
        }
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .status(category.getStatus() != null ? category.getStatus().name() : null)
                .displayOrder(category.getDisplayOrder())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(childrenDTOs)
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    
    private CategoryDTO toDTOWithChildrenRecursive(Category category) {
        int productCount = (int) productRepository.countByCategoryId(category.getId());
        
        List<CategoryDTO> childrenDTOs = new ArrayList<>();
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrder(category.getId());
        for (Category child : children) {
            if (child.getStatus() == CategoryStatus.ACTIVE) {
                childrenDTOs.add(toDTOWithChildrenRecursive(child));
            }
        }
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .status(category.getStatus() != null ? category.getStatus().name() : null)
                .displayOrder(category.getDisplayOrder())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(childrenDTOs)
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
