package com.laundry.repository;


import com.laundry.entity.Payment;
import com.laundry.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrderId(Integer orderId);
    List<Payment> findByStatus(PaymentStatus status);
    boolean existsByOrderId(Integer orderId);
    boolean existsByAmount(Double amount);
    Optional<Payment> findByAmount(Double amount);
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    List<Payment> findPaymentsByOrderId(@Param("orderId") Integer orderId);
}
