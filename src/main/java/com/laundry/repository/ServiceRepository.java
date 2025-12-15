package com.laundry.repository;


import com.laundry.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Integer> {
    Optional<Services> findByName(String name);
    List<Services> findByUnitType(String unitType);
    boolean existsByName(String name);
    boolean existsByPricePerUnit(Double pricePerUnit);
    Optional<Services> findByPricePerUnit(Double pricePerUnit);
}