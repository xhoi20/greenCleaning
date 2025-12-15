package com.laundry.service.serviceInterface;


import com.laundry.dto.OrderCreateDTO;
import com.laundry.dto.OrderUpdateDTO;
import com.laundry.entity.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    ResponseEntity<?> createOrder(OrderCreateDTO request);
    ResponseEntity<?> updateOrder(Integer id, OrderUpdateDTO updateRequest);
    Optional<Order> getOrderById(Integer id);
    Iterable<Order> getAllOrders();
    void deleteOrderById(Integer id);
    // Metoda specifike: p.sh., gjej orders sipas customer ose status
    List<Order> getOrdersByCustomerId(Integer customerId);
    List<Order> getOrdersByStatus(String status);
    Optional<Order> getOrderWithDetails(Integer id);
}