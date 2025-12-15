package com.laundry.service.serviceInterface;

import com.laundry.dto.OrderServiceCreateDTO;
import com.laundry.dto.OrderServiceUpdateDTO;
import com.laundry.entity.OrderService;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IOrderServiceService {
    ResponseEntity<Map<String, Object>> createOrderService(OrderServiceCreateDTO request);
    OrderService createOrderServiceEntity(OrderServiceCreateDTO dto);
    ResponseEntity<Map<String, Object>> updateOrderService(Integer id, OrderServiceUpdateDTO updateRequest);
    Optional<OrderService> getOrderServiceById(Integer id);
    Iterable<OrderService> getAllOrderServices();
    void deleteOrderServiceById(Integer id);
    List<OrderService> getByOrderItemId(Integer orderItemId);
    List<OrderService> getByServiceId(Integer serviceId);
}