package com.laundry.repository;


import com.laundry.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByPhoneContaining(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Customer> findByPhone(String phone);
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o WHERE o.createdAt > :threshold")
    List<Customer> findByOrdersCreatedAfter(@Param("threshold") LocalDateTime threshold);
}
