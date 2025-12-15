package com.laundry.repository;


import com.laundry.entity.InventoryTransaction;
import com.laundry.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
    Optional<InventoryTransaction> findBySupplyId(Integer supplyId);
    List<InventoryTransaction> findByTransactionType(TransactionType type);
    boolean existsBySupplyId(Integer supplyId);
    boolean existsByOrderId(Integer orderId);
    Optional<InventoryTransaction> findByOrderId(Integer orderId);
    List<InventoryTransaction> findBySupplyId(int supplyId);

}