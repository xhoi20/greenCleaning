package com.laundry.repository;


import com.laundry.entity.Order;
import com.laundry.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomerId(Integer customerId);
    List<Order> findByStatus(OrderStatus status);
    boolean existsByCustomerId(Integer customerId);
    boolean existsByDropoffDate(LocalDate dropoffDate);
    Optional<Order> findByDropoffDate(LocalDate dropoffDate);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer")  // Ngarkon customer me order në një query
    List<Order> findAllWithCustomer();
    // Query e re për listën: Eager load të gjitha collections
    @EntityGraph(attributePaths = {"orderItems", "orderServices", "customer"})
    List<Order> findAll();

    // Nëse ke query custom për ID, shto edhe atë
    @EntityGraph(attributePaths = {"orderItems", "orderServices", "customer"})
    Optional<Order> findById(Long id);
    @Query("""
        select distinct o
        from Order o
        left join fetch o.orderItems oi
        left join fetch o.customer c
        left join fetch oi.orderServices os
        left join fetch o.payments p
        left join fetch o.orderEmployees oe
        left join fetch oe.employee e
             left join fetch os.service s
        where o.id = :id
    """)
    Optional<Order> findByIdWithAllRelations(@Param("id") Integer id);
}