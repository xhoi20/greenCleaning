package com.laundry.service.serviceInterface;


import com.laundry.dto.OrderItemCreateDTO;
import com.laundry.dto.OrderItemUpdateDTO;
import com.laundry.entity.OrderItem;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IOrderItemService {
    ResponseEntity<Map<String, Object>> createOrderItem(OrderItemCreateDTO request);
    OrderItem createOrderItemInternal(OrderItemCreateDTO request);
    ResponseEntity<Map<String, Object>> updateOrderItem(Integer id, OrderItemUpdateDTO updateRequest);
    Optional<OrderItem> getOrderItemById(Integer id);
    Iterable<OrderItem> getAllOrderItems();
    void deleteOrderItemById(Integer id);
    List<OrderItem> getOrderItemsByOrderId(Integer orderId);
    Optional<OrderItem> getOrderItemByTagId(String tagId);
}