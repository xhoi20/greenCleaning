package com.laundry.service.serviceInterface;



import com.laundry.dto.OrderEmployeeCreateDTO;
import com.laundry.dto.OrderEmployeeUpdateDTO;
import com.laundry.entity.OrderEmployee;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IOrderEmployeeService {
    ResponseEntity<Map<String, Object>> createOrderEmployee(OrderEmployeeCreateDTO request);
    ResponseEntity<Map<String, Object>> updateOrderEmployee(Integer id, OrderEmployeeUpdateDTO updateRequest);
    Optional<OrderEmployee> getOrderEmployeeById(Integer id);
    Iterable<OrderEmployee> getAllOrderEmployees();
    void deleteOrderEmployeeById(Integer id);
    List<OrderEmployee> getByOrderId(Integer orderId);
    List<OrderEmployee> getByEmployeeId(Integer employeeId);
}