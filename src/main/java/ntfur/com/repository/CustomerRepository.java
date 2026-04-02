package ntfur.com.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByUserEmail(String email);

    List<Customer> findByGender(Customer.Gender gender);

    @Query("""
    	    SELECT c FROM Customer c 
    	    LEFT JOIN c.user u
    	    WHERE 
    	        LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
    	        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    	        OR c.phone LIKE CONCAT('%', :keyword, '%')
    	""")    
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Customer c ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersBySpent();

    @Query("SELECT c FROM Customer c ORDER BY c.totalOrders DESC")
    List<Customer> findTopCustomersByOrders();

    @Query("SELECT c FROM Customer c WHERE c.totalOrders >= :minOrders")
    List<Customer> findByMinOrders(@Param("minOrders") int minOrders);

    @Query("SELECT c FROM Customer c WHERE c.totalSpent >= :minSpent")
    List<Customer> findByMinSpent(@Param("minSpent") BigDecimal minSpent);

    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();

    @Query("SELECT SUM(c.totalSpent) FROM Customer c")
    BigDecimal getTotalRevenue();

    @Query("""
    	    SELECT c FROM Customer c
    	    JOIN c.user u
    	    WHERE u.createdAt BETWEEN :startDate AND :endDate
    	""")
    List<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
