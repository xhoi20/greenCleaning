package com.laundry.repository;

import com.laundry.entity.OrderService;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderServiceRepository extends JpaRepository<OrderService, Integer> {

    // Query method për të gjetur sipas OrderItem ID (përdor _ për nested property)
    List<OrderService> findByOrderItem_Id(Integer orderItemId);

    // Query method për të gjetur sipas Service ID
    List<OrderService> findByService_Id(Integer serviceId);

    // Alternativë me @Query nëse method naming nuk funksionon (p.sh. për kompleksitet)
    @Query("SELECT os FROM OrderService os WHERE os.orderItem.id = :orderItemId")
    List<OrderService> findByOrderItemId(@Param("orderItemId") Integer orderItemId);

    @Query("SELECT os FROM OrderService os WHERE os.service.id = :serviceId")
    List<OrderService> findByServiceId(@Param("serviceId") Integer serviceId);
}