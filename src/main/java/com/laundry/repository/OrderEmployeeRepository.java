package com.laundry.repository;


import com.laundry.entity.OrderEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderEmployeeRepository extends JpaRepository<OrderEmployee, Integer> {
    // Query method për të gjetur sipas order ID (përdor _ për nested property)
    List<OrderEmployee> findByOrder_Id(Integer orderId);

    // Query method për të gjetur sipas employee ID
    List<OrderEmployee> findByEmployee_Id(Integer employeeId);

    // Alternativë me @Query nëse method naming nuk funksionon (p.sh. për kompleksitet)
    @Query("SELECT oe FROM OrderEmployee oe WHERE oe.order.id = :orderId")
    List<OrderEmployee> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT oe FROM OrderEmployee oe WHERE oe.employee.id = :employeeId")
    List<OrderEmployee> findByEmployeeId(@Param("employeeId") Integer employeeId);
}