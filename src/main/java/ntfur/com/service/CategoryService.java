package ntfur.com.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Category;
import ntfur.com.entity.Category.CategoryStatus;
import ntfur.com.entity.dto.CreateCategoryRequest;
import ntfur.com.entity.dto.category.CategoryDTO;
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

    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByStatusOrderByDisplayOrder(CategoryStatus.ACTIVE).stream()
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
        return toDTO(category);
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
        category.setIcon(request.getIcon());
        
        if (request.getStatus() != null) {
            category.setStatus(CategoryStatus.valueOf(request.getStatus()));
        } else {
            category.setStatus(CategoryStatus.ACTIVE);
        }
        
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        
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
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getStatus() != null) {
            category.setStatus(CategoryStatus.valueOf(request.getStatus()));
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category updatedCategory = categoryRepository.save(category);
        return toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục với id: " + id);
        }
        
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa danh mục vì có " + productCount + " sản phẩm đang thuộc danh mục này");
        }
        
        categoryRepository.deleteById(id);
    }

    public long countCategories() {
        return categoryRepository.count();
    }

    private CategoryDTO toDTO(Category category) {
        int productCount = (int) productRepository.countByCategoryId(category.getId());
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .status(category.getStatus() != null ? category.getStatus().name() : null)
                .displayOrder(category.getDisplayOrder())
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
