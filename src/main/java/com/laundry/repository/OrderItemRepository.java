package com.laundry.repository;


import com.laundry.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Optional<OrderItem> findByTagId(String tagId);
    List<OrderItem> findByOrderId(Integer orderId);
    boolean existsByTagId(String tagId);
    boolean existsByItemDescription(String itemDescription);
    Optional<OrderItem> findByItemDescription(String itemDescription);
}