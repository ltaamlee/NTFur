package ntfur.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ntfur.com.entity.CustomerAddress;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);

    Optional<CustomerAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);

    @Modifying
    @Query("UPDATE CustomerAddress ca SET ca.isDefault = false WHERE ca.customer.id = :customerId AND ca.isDefault = true")
    void clearDefaultForCustomer(@Param("customerId") Long customerId);

    void deleteByIdAndCustomerId(Long id, Long customerId);

    boolean existsByIdAndCustomerId(Long id, Long customerId);
}
