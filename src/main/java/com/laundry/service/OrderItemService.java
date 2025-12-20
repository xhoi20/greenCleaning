package com.laundry.service;

import com.laundry.dto.OrderItemCreateDTO;
import com.laundry.dto.OrderItemUpdateDTO;
import com.laundry.dto.OrderUpdateDTO;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.repository.OrderItemRepository;
import com.laundry.service.serviceInterface.IOrderItemService;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderItemService extends BaseService implements IOrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    @Lazy
    private OrderService orderService;  // Për gjetje order nga ID dhe përditësim totali

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    @Transactional
    public OrderItem createOrderItemInternal(OrderItemCreateDTO request) {

        // Validime bazë (të njëjtat si në createOrderItem)
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID is missing or empty");
        }
        if (request.getItemDescription() == null || request.getItemDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Item description is missing or empty");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }

        // Kontrollo nëse Order ekziston
        Optional<Order> orderOpt = orderService.getOrderById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order with this ID does not exist");
        }
        Order order = orderOpt.get();
        getAuthenticatedEmployee();

       // getAuthenticatedUser();

        // Krijo entity të re nga DTO
        OrderItem newItem = new OrderItem();
        newItem.setOrder(order);
        newItem.setItemDescription(request.getItemDescription());
        newItem.setQuantity(request.getQuantity());
        newItem.setUnitPrice(request.getUnitPrice());
        newItem.setSubtotal(request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity())));
        newItem.setTagId(generateTagId(request.getItemDescription(), order.getId()));

        // Ruaj entity-n
        OrderItem savedItem = orderItemRepository.save(newItem);

        // Përditëso totalin e Order-it
        updateOrderTotal(order);

        return savedItem;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createOrderItem(OrderItemCreateDTO request) {
        try {
            // Validime bazë
            if (request.getOrderId() == null) {
                return createErrorResponse("Order ID is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getItemDescription() == null || request.getItemDescription().trim().isEmpty()) {
                return createErrorResponse("Item description is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getQuantity() <= 0) {
                return createErrorResponse("Quantity must be greater than 0", HttpStatus.BAD_REQUEST);
            }
            if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                return createErrorResponse("Unit price must be non-negative", HttpStatus.BAD_REQUEST);
            }

            // Kontrollo nëse Order ekziston
            Optional<Order> orderOpt = orderService.getOrderById(request.getOrderId());
            if (orderOpt.isEmpty()) {
                return createErrorResponse("Order with this ID does not exist", HttpStatus.BAD_REQUEST);
            }
            Order order = orderOpt.get();

            getAuthenticatedUser();  // Kontrollo autentikimin si në OrderService

            // Krijo entity të re nga DTO
            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setItemDescription(request.getItemDescription());
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(request.getUnitPrice());
            newItem.setSubtotal(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));  // Llogarit subtotal
            newItem.setTagId(generateTagId(request.getItemDescription(), order.getId()));  // Genero tag për lavanderi

            // Ruaj entity-n
            OrderItem savedItem = orderItemRepository.save(newItem);

            // Përditëso totalin e Order-it
            updateOrderTotal(order);

            return createSuccessResponse(savedItem, "OrderItem created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> updateOrderItem(Integer id, OrderItemUpdateDTO updateRequest) {
        try {
            Optional<OrderItem> itemOpt = orderItemRepository.findById(id);
            if (itemOpt.isPresent()) {
                OrderItem item = itemOpt.get();
                Order order = item.getOrder();  // Merr order-in për të përditësuar totalin më vonë

                // Validime...
                if (updateRequest.getItemDescription() != null && !updateRequest.getItemDescription().trim().isEmpty()) {
                    item.setItemDescription(updateRequest.getItemDescription());
                }
                if (updateRequest.getQuantity() > 0) {
                    item.setQuantity(updateRequest.getQuantity());
                }
                if (updateRequest.getUnitPrice() != null && updateRequest.getUnitPrice().compareTo(BigDecimal.ZERO) >= 0) {
                    item.setUnitPrice(updateRequest.getUnitPrice());
                }
                // Rillogarit subtotal-in
                item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                // Përditëso tagId nëse ndryshon description
                if (updateRequest.getItemDescription() != null) {
                    item.setTagId(generateTagId(updateRequest.getItemDescription(), order.getId()));
                }

                getAuthenticatedUser();
                OrderItem updatedItem = orderItemRepository.save(item);

                // Përditëso totalin e Order-it
                updateOrderTotal(order);

                return createSuccessResponse(updatedItem, "OrderItem updated successfully", HttpStatus.OK);
            } else {
                return createErrorResponse("OrderItem not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<OrderItem> getOrderItemById(Integer id) {
        return orderItemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Iterable<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Transactional
    public void deleteOrderItemById(Integer id) {
        getAuthenticatedUser();
        Optional<OrderItem> itemOpt = orderItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("OrderItem with ID " + id + " not found.");
        }
        OrderItem itemToDelete = itemOpt.get();
        Order order = itemToDelete.getOrder();
        orderItemRepository.deleteById(id);

        // Përditëso totalin e Order-it pas fshirjes
        updateOrderTotal(order);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<OrderItem> getOrderItemByTagId(String tagId) {
        return orderItemRepository.findByTagId(tagId);
    }

    /**
     * Përditëson totalin e Order-it bazuar në subtotal-et e OrderItem-ëve.
     * E thirr pas krijimit/përditësimit/fshirjes së item-ëve.
     */
    private void updateOrderTotal(Order order) {
        List<OrderItem> items = getOrderItemsByOrderId(order.getId());
        BigDecimal newTotal = items.stream()
                .map(OrderItem::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(newTotal);
        orderService.updateOrder(order.getId(), new OrderUpdateDTO().toBuilder()
                .totalAmount(newTotal)
                .build());
    }

    /**
     * Generon një tagId unik për OrderItem (për gjurmim në lavanderi).
     */
    private String generateTagId(String description, int orderId) {
        return "TAG-" + orderId + "-" + description.substring(0, Math.min(10, description.length())) + "-" + System.currentTimeMillis();
    }
}