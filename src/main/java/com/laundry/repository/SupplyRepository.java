package com.laundry.repository;



import com.laundry.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Integer> {
    Optional<Supply> findByName(String name);
    List<Supply> findByUnit(String unit);
    boolean existsByName(String name);
    boolean existsByCurrentStockLessThan(Integer reorderLevel);
    List<Supply> findByCurrentStockLessThan(Integer threshold);
}