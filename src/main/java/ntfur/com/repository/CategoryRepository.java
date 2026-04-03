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
    
    @Query("SELECT c FROM Category c WHERE c.status = :status ORDER BY c.displayOrder ASC, c.createdAt DESC")
    List<Category> findByStatusOrderByDisplayOrderAndCreatedAt(@Param("status") CategoryStatus status);
    
    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.createdAt DESC")
    List<Category> findAllOrderByDisplayOrderAndCreatedAt();
    
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();
    
    @Query("SELECT c FROM Category c ORDER BY c.createdAt DESC")
    List<Category> findAllOrderByCreatedAt();
}
