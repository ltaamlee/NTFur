package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.ProductSet;

@Repository
public interface ProductSetRepository extends JpaRepository<ProductSet, Long> {

    Optional<ProductSet> findBySlug(String slug);

    List<ProductSet> findByActiveTrue();

    @Query("SELECT ps FROM ProductSet ps WHERE LOWER(ps.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProductSet> searchByKeyword(String keyword);
}
