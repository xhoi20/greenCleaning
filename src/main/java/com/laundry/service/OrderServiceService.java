package com.laundry.service;

import com.laundry.dto.OrderServiceCreateDTO; // DTO me orderItemId, serviceId, quantity, price
import com.laundry.dto.OrderServiceUpdateDTO; // DTO me quantity, price (për update)
import com.laundry.entity.OrderService;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Services; // Shtuar import për entity Services (plural, si në entity OrderService)
import com.laundry.repository.OrderItemRepository;
import com.laundry.repository.OrderServiceRepository; // Asumo ekziston me extends JpaRepository
import com.laundry.repository.ServiceRepository;
import com.laundry.service.OrderItemService;
import com.laundry.service.ServiceService; // ServiceService për entitetin Services
import com.laundry.service.serviceInterface.IOrderServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.laundry.entity.Services;

@Service
public class OrderServiceService extends BaseService implements IOrderServiceService {
    private final OrderServiceRepository orderServiceRepository;
    private final OrderItemRepository orderItemRepository;
    private final ServiceRepository serviceRepository;

    public OrderServiceService(OrderServiceRepository orderServiceRepository,
                               OrderItemRepository orderItemRepository,
                               ServiceRepository serviceRepository) {
        this.orderServiceRepository = orderServiceRepository;
        this.orderItemRepository = orderItemRepository;
        this.serviceRepository = serviceRepository;
    }
//    private final OrderServiceRepository orderServiceRepository;
//
    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ServiceService serviceService;
//
//    public OrderServiceService(OrderServiceRepository orderServiceRepository) {
//        this.orderServiceRepository = orderServiceRepository;
//    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createOrderService(OrderServiceCreateDTO request) {
        try {
            if (request.getOrderItemId() == null) {
                return createErrorResponse("Order Item ID is missing", HttpStatus.BAD_REQUEST);
            }
            if (request.getServiceId() == null) {
                return createErrorResponse("Service ID is missing", HttpStatus.BAD_REQUEST);
            }
            if (request.getQuantity() <= 0) {
                return createErrorResponse("Invalid quantity", HttpStatus.BAD_REQUEST);
            }
            if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return createErrorResponse("Invalid price", HttpStatus.BAD_REQUEST);
            }

            Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(request.getOrderItemId());
            if (orderItemOpt.isEmpty()) {
                return createErrorResponse("Order Item not found", HttpStatus.NOT_FOUND);
            }

            Optional<Services> serviceOpt = serviceService.getServiceById(request.getServiceId());  // Ndryshuar në Services
            if (serviceOpt.isEmpty()) {
                return createErrorResponse("Service not found", HttpStatus.NOT_FOUND);
            }

            getAuthenticatedUser();

            OrderService newOrderService = OrderService.builder()
                    .orderItem(orderItemOpt.get())
                    .service(serviceOpt.get())  // Tani tipet përputhen (Services entity)
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .build();

            OrderService savedOrderService = orderServiceRepository.save(newOrderService);
            return createSuccessResponse(savedOrderService, "Order Service created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }



    @Transactional
    public OrderService createOrderServiceEntity(OrderServiceCreateDTO dto) {

        // 1) gjej OrderItem nga ID
        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found: " + dto.getOrderItemId()));

        // 2) gjej Service nga ID
        Services service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + dto.getServiceId()));

        // 3) krijo entity
        OrderService os = new OrderService();
        os.setOrderItem(orderItem);
        os.setService(service);

       // int qty = (dto.getQuantity() != null && dto.getQuantity() > 0) ? dto.getQuantity() : 1;
//        os.setQuantity(qty);
        int qty = dto.getQuantity() > 0 ? dto.getQuantity() : 1;

        // nëse price s’vjen nga UI, llogarite nga service.pricePerUnit
        BigDecimal price;
        if (dto.getPrice() != null) {
            price = dto.getPrice();
        } else {
            BigDecimal perUnit = service.getPricePerUnit() != null
                    ? service.getPricePerUnit()
                    : BigDecimal.ZERO;
            price = perUnit.multiply(BigDecimal.valueOf(qty));
        }
        os.setPrice(price);

        // ruaj edhe emrin e shërbimit në kolonën service_name
        os.setServiceName(service.getName());

        return orderServiceRepository.save(os);
    }
    @Transactional
    public ResponseEntity<Map<String, Object>> updateOrderService(Integer id, OrderServiceUpdateDTO updateRequest) {
        try {
            getAuthenticatedUser();
            Optional<OrderService> orderServiceOpt = orderServiceRepository.findById(id);
            if (orderServiceOpt.isPresent()) {
                OrderService orderService = orderServiceOpt.get();
                if (updateRequest.getQuantity() != null && updateRequest.getQuantity() > 0) {
                    orderService.setQuantity(updateRequest.getQuantity());
                }
                if (updateRequest.getPrice() != null && updateRequest.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    orderService.setPrice(updateRequest.getPrice());
                }
                return createSuccessResponse(orderServiceRepository.save(orderService), "Order Service updated successfully", HttpStatus.OK);
            }
            return createErrorResponse("Order Service not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<OrderService> getOrderServiceById(Integer id) {
        return orderServiceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Iterable<OrderService> getAllOrderServices() {
        return orderServiceRepository.findAll();
    }

    @Transactional
    public void deleteOrderServiceById(Integer id) {
        getAuthenticatedUser();
        if (!orderServiceRepository.existsById(id)) {
            throw new RuntimeException("Order Service with ID " + id + " not found.");
        }
        orderServiceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderService> getByOrderItemId(Integer orderItemId) {
        return orderServiceRepository.findByOrderItem_Id(orderItemId); // Asumo query method
    }

    @Transactional(readOnly = true)
    public List<OrderService> getByServiceId(Integer serviceId) {
        return orderServiceRepository.findByService_Id(serviceId); // Asumo query method
    }
}