package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Category;
import ntfur.com.entity.Category.CategoryStatus;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    List<Category> findByStatus(CategoryStatus status);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);

    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC")
    List<Category> findAllOrderByDisplayOrder();

    @Query("SELECT c FROM Category c WHERE c.status = :status ORDER BY c.displayOrder ASC")
    List<Category> findByStatusOrderByDisplayOrder(@Param("status") CategoryStatus status);
    
    // Nested categories methods
    List<Category> findByParentIsNullOrderByDisplayOrder();
    
    List<Category> findByParentIdOrderByDisplayOrder(Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.status = :status ORDER BY c.displayOrder ASC")
    List<Category> findByParentIsNullAndStatusOrderByDisplayOrder(@Param("status") CategoryStatus status);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parent.id = :parentId")
    boolean hasChildren(@Param("parentId") Long parentId);
    
    // Recursive query to get all child IDs
    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "SELECT id FROM categories WHERE parent_id = :parentId " +
            "UNION ALL " +
            "SELECT c.id FROM categories c " +
            "INNER JOIN category_tree ct ON c.parent_id = ct.id " +
            ") SELECT id FROM category_tree", nativeQuery = true)
    List<Long> findAllChildIds(@Param("parentId") Long parentId);
}
