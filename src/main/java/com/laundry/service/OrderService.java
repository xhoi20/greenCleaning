package com.laundry.service;

import com.laundry.dto.*;
import com.laundry.entity.*;
import com.laundry.entity.Order;
import com.laundry.repository.OrderRepository;
import com.laundry.service.serviceInterface.IOrderService;
//import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class OrderService extends BaseService implements IOrderService {
    @Autowired
    private PaymentService paymentService;
    private final OrderRepository orderRepository;
    @Autowired
    private OrderEmployeeService orderEmployeeService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CustomerService customerService;  // Për gjetje customer nga ID
    @Autowired
    private OrderServiceService orderServiceService;
     @Autowired private OrderItemService orderItemService;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Transactional
    public ResponseEntity<Map<String, Object>> createOrder(OrderCreateDTO request) {
        try {
            // --- VALIDATIONS ---
            if (request.getCustomerId() == null)
                return createErrorResponse("Customer ID is missing or empty", HttpStatus.BAD_REQUEST);
            if (request.getDropoffDate() == null)
                return createErrorResponse("Dropoff date is missing or empty", HttpStatus.BAD_REQUEST);

            Optional<Customer> customerOpt = customerService.getCustomerById(request.getCustomerId());
            if (customerOpt.isEmpty())
                return createErrorResponse("Customer with this ID does not exist", HttpStatus.BAD_REQUEST);

            Customer customer = customerOpt.get();
            //getAuthenticatedUser(); // keep if you use it for security checks

            // --- CREATE ORDER (WITHOUT TOTAL YET) ---
            Order newOrder = new Order();
            newOrder.setCustomer(customer);
            newOrder.setDropoffDate(request.getDropoffDate());
            newOrder.setPickupDate(request.getPickupDate());
            newOrder.setStatus(OrderStatus.RECEIVED);
            newOrder.setNotes(request.getNotes());
            newOrder.setCreatedAt(LocalDateTime.now());
            newOrder.setUpdatedAt(LocalDateTime.now());
            newOrder.setTotalAmount(BigDecimal.ZERO);

            Order savedOrder = orderRepository.save(newOrder);

            // --- GET LOGGED-IN EMPLOYEE FROM TOKEN ---
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            String firstName = auth.getName();
            Optional<Employee> employeeOpt = employeeService.getEmployeeByName(firstName);
            if (employeeOpt.isEmpty()) {
                return createErrorResponse("Employee for logged-in user not found", HttpStatus.BAD_REQUEST);
            }
            Employee employee = employeeOpt.get();

            // --- CREATE ORDER_EMPLOYEE ASSIGNMENT ---
            OrderEmployeeCreateDTO assignmentDto = new OrderEmployeeCreateDTO();
            assignmentDto.setOrderId(savedOrder.getId());
            assignmentDto.setEmployeeId(employee.getId());
            assignmentDto.setAssignedRole(AssignedRole.ADMIN); // or RECEIVER, etc.

            ResponseEntity<Map<String, Object>> assignmentResponse =
                    orderEmployeeService.createOrderEmployee(assignmentDto);

            if (!assignmentResponse.getStatusCode().is2xxSuccessful()) {
                // stop & return error if assignment failed
                return assignmentResponse;
            }

            // --- CREATE ITEMS ---
            Map<Integer, Integer> itemIndexToId = new HashMap<>();
            BigDecimal itemsTotal = BigDecimal.ZERO;

            if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
                for (int i = 0; i < request.getOrderItems().size(); i++) {
                    OrderItemCreateDTO itemDTO = request.getOrderItems().get(i);
                    itemDTO.setOrderId(savedOrder.getId());
                    OrderItem savedItem = orderItemService.createOrderItemInternal(itemDTO);
                    itemIndexToId.put(i, savedItem.getId());

                    // accumulate total
                    if (itemDTO.getUnitPrice() != null) {
                        BigDecimal subtotal = itemDTO.getUnitPrice()
                                .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                        itemsTotal = itemsTotal.add(subtotal);
                    }
                }
            }

            // --- CREATE SERVICES ---
            BigDecimal servicesTotal = BigDecimal.ZERO;
            if (request.getOrderServices() != null && !request.getOrderServices().isEmpty()) {
                for (OrderServiceCreateDTO serviceDTO : request.getOrderServices()) {
                    if (serviceDTO.getServiceId() == null) continue;
                    Integer index = serviceDTO.getOrderItemId();
                    if (index == null) continue;

                    Integer realOrderItemId = itemIndexToId.get(index);
                    if (realOrderItemId == null) continue;

                    serviceDTO.setOrderItemId(realOrderItemId);
                    orderServiceService.createOrderService(serviceDTO);

                    // accumulate total
                    if (serviceDTO.getPrice() != null) {
                        BigDecimal subtotal = serviceDTO.getPrice()
                                .multiply(BigDecimal.valueOf(serviceDTO.getQuantity()));
                        servicesTotal = servicesTotal.add(subtotal);
                    }
                }
            }

            // --- FINAL TOTAL CALCULATION ---
            BigDecimal finalTotal = itemsTotal.add(servicesTotal);
            savedOrder.setTotalAmount(finalTotal);
            savedOrder.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(savedOrder);
            // ================================
            // === CREATE PAYMENT HERE ========
            // ================================
            PaymentCreateDTO paymentDTO = new PaymentCreateDTO();
            paymentDTO.setOrderId(savedOrder.getId());
            paymentDTO.setAmount(finalTotal);                     // save full amount as payment
            paymentDTO.setPaymentMethod(request.getPaymentMethod());

            ResponseEntity<Map<String, Object>> paymentResponse =
                    paymentService.createPayment(paymentDTO);

            if (!paymentResponse.getStatusCode().is2xxSuccessful()) {
                // force rollback of whole transaction
                throw new RuntimeException(
                        "Payment failed: " + paymentResponse.getBody().get("error")
                );
            }
            return createSuccessResponse(savedOrder, "Order created successfully", HttpStatus.CREATED);

        } catch (Exception e) {
            return handleException(e);
        }
    }



    @Transactional
    public ResponseEntity<Map<String, Object>> updateOrder(Integer id, OrderUpdateDTO updateRequest) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();

                // Validime...
                if (updateRequest.getPickupDate() != null) {
                    order.setPickupDate(updateRequest.getPickupDate());
                }
                if (updateRequest.getTotalAmount() != null) {
                    order.setTotalAmount(updateRequest.getTotalAmount());
                }
                if (updateRequest.getNotes() != null && !updateRequest.getNotes().trim().isEmpty()) {
                    order.setNotes(updateRequest.getNotes());
                }
                if (updateRequest.getStatus() != null) {
                    order.setStatus(OrderStatus.valueOf(updateRequest.getStatus().toUpperCase()));
                }
                order.setUpdatedAt(LocalDateTime.now());  // Vetëm updatedAt në entity
                getAuthenticatedUser();
                Order updatedOrder = orderRepository.save(order);

                return createSuccessResponse(updatedOrder, "Order updated successfully", HttpStatus.OK);
            } else {
                return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

@Transactional(readOnly = true)
public Iterable<Order> getAllOrders() {
    return orderRepository.findAllWithCustomer();
}

    @Transactional
    public void deleteOrderById(Integer id) {
        getAuthenticatedUser();
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order with ID " + id + " not found.");
        }

        orderRepository.deleteById(id);
    }

    @Transactional
    public List<Order> getOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()));
    }
    public Optional<Order> getOrderWithDetails(Integer id) {
        return orderRepository.findByIdWithAllRelations(id);
    }

}

//package com.laundry.service;
//
//import com.laundry.dto.*;
//import com.laundry.entity.Order;
//import com.laundry.entity.Customer;
//import com.laundry.entity.Order;
//import com.laundry.entity.OrderItem;
//import com.laundry.entity.OrderStatus;
//import com.laundry.repository.OrderRepository;
//import com.laundry.service.serviceInterface.IOrderService;
////import jakarta.transaction.Transactional;
//import org.hibernate.Hibernate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class OrderService extends BaseService implements IOrderService {
//
//    private final OrderRepository orderRepository;
//
//    @Autowired
//    private CustomerService customerService;  // Për gjetje customer nga ID
//
//    @Autowired
//    private OrderItemService orderItemService;
//
//    @Autowired
//    private OrderEmployeeService orderEmployeeService;  // Shtuar për të krijuar lidhje me punonjësit
//
//    @Autowired
//    private PaymentService paymentService;  // Shtuar për të krijuar pagesat
//
//    @Autowired
//    private ServiceService serviceService;  // Shtuar për të krijuar shërbimet (p.sh., llojet e larjes)
//
//    public OrderService(OrderRepository orderRepository) {
//        this.orderRepository = orderRepository;
//    }
//
//
//    @Transactional(readOnly = true)
//    public ResponseEntity<Map<String, Object>> createOrder(OrderCreateDTO request) {
//        try {
//            // Validime bazë (si në CustomerService)
//            if (request.getCustomerId() == null) {
//                return createErrorResponse("Customer ID is missing or empty", HttpStatus.BAD_REQUEST);
//            }
//            if (request.getDropoffDate() == null) {
//                return createErrorResponse("Dropoff date is missing or empty", HttpStatus.BAD_REQUEST);
//            }
//
//            // Kontrollo nëse Customer ekziston
//            Optional<Customer> customerOpt = customerService.getCustomerById(request.getCustomerId());
//            if (customerOpt.isEmpty()) {
//                return createErrorResponse("Customer with this ID does not exist", HttpStatus.BAD_REQUEST);
//            }
//            Customer customer = customerOpt.get();
//            getAuthenticatedUser();
//
//            // Krijo entity të re nga DTO DHE set-o timestamps KËTU
//            Order newOrder = new Order();
//            newOrder.setCustomer(customer);
//            newOrder.setDropoffDate(request.getDropoffDate());
//            newOrder.setPickupDate(request.getPickupDate());
//            newOrder.setStatus(OrderStatus.RECEIVED);
//            newOrder.setTotalAmount(BigDecimal.ZERO);
//            newOrder.setNotes(request.getNotes());
//            newOrder.setCreatedAt(LocalDateTime.now());
//            newOrder.setUpdatedAt(LocalDateTime.now());
//
//            // Ruaj entity-n
//            Order savedOrder = orderRepository.save(newOrder);
//
//            // Krijo Order Items (si më parë)
//            if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
//                for (OrderItemCreateDTO itemDTO : request.getOrderItems()) {
//                    // Set-o orderId në DTO për ta kaluar në service
//                    itemDTO.setOrderId(savedOrder.getId());
//                    // Krijo item-in – OrderItemService do të lidhë, llogarisë subtotal dhe përditësojë totalin
//                    orderItemService.createOrderItem(itemDTO);
//                }
//            }
//
//            // Krijo Order Employees (supozojmë se OrderCreateDTO ka List<OrderEmployeeCreateDTO> getOrderEmployees())
//            if (request.getOrderEmployees() != null && !request.getOrderEmployees().isEmpty()) {
//                for (OrderEmployeeCreateDTO employeeDTO : request.getOrderEmployees()) {
//                    // Set-o orderId në DTO për ta kaluar në service
//                    employeeDTO.setOrderId(savedOrder.getId());
//                    // Krijo lidhjen me punonjës – OrderEmployeeService do të ruajë në tabelën e vet
//                    orderEmployeeService.createOrderEmployee(employeeDTO);
//                }
//            }
//
//            // Krijo Payments (supozojmë se OrderCreateDTO ka List<PaymentCreateDTO> getPayments())
//            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
//                for (PaymentCreateDTO paymentDTO : request.getPayments()) {
//                    // Set-o orderId në DTO për ta kaluar në service
//                    paymentDTO.setOrderId(savedOrder.getId());
//                    // Krijo pagesën – PaymentService do të ruajë në tabelën e vet
//                    paymentService.createPayment(paymentDTO);
//                }
//            }
//
//            // Krijo Services (supozojmë se OrderCreateDTO ka List<ServiceCreateDTO> getServices())
//            if (request.getServices() != null && !request.getServices().isEmpty()) {
//                for (ServiceCreateDTO serviceDTO : request.getServices()) {
//                    // Set-o orderId në DTO për ta kaluar në service
//                    serviceDTO.setOrderId(savedOrder.getId());
//                    // Krijo shërbimin – ServiceService do të ruajë në tabelën e vet
//                    serviceService.createService(serviceDTO);
//                }
//            }
//
//            // Përditëso order-in final pas krijimit të të gjitha lidhjeve (p.sh., për të llogaritur totalin nëse nevojitet)
//            Order finalOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);
//
//            return createSuccessResponse(finalOrder, "Order created successfully with related entities", HttpStatus.CREATED);
//        } catch (Exception e) {
//            return handleException(e);
//        }
//    }
//
//    @Transactional
//
//    public ResponseEntity<Map<String, Object>> updateOrder(Integer id, OrderUpdateDTO updateRequest) {
//        try {
//            Optional<Order> orderOpt = orderRepository.findById(id);
//            if (orderOpt.isPresent()) {
//                Order order = orderOpt.get();
//
//                // Validime...
//                if (updateRequest.getPickupDate() != null) {
//                    order.setPickupDate(updateRequest.getPickupDate());
//                }
//                if (updateRequest.getTotalAmount() != null) {
//                    order.setTotalAmount(updateRequest.getTotalAmount());
//                }
//                if (updateRequest.getNotes() != null && !updateRequest.getNotes().trim().isEmpty()) {
//                    order.setNotes(updateRequest.getNotes());
//                }
//                if (updateRequest.getStatus() != null) {
//                    order.setStatus(OrderStatus.valueOf(updateRequest.getStatus().toUpperCase()));
//                }
//                order.setUpdatedAt(LocalDateTime.now());  // Vetëm updatedAt në entity
//                getAuthenticatedUser();
//                Order updatedOrder = orderRepository.save(order);
//
//                return createSuccessResponse(updatedOrder, "Order updated successfully", HttpStatus.OK);
//            } else {
//                return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            return handleException(e);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<Order> getOrderById(Integer id) {
//        return orderRepository.findById(id);
//    }
//
//    @Transactional(readOnly = true)
//    public Iterable<Order> getAllOrders() {
//        return orderRepository.findAllWithCustomer();
//    }
//
//    @Transactional
//    public void deleteOrderById(Integer id) {
//        getAuthenticatedUser();
//        if (!orderRepository.existsById(id)) {
//            throw new RuntimeException("Order with ID " + id + " not found.");
//        }
//
//        orderRepository.deleteById(id);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Order> getOrdersByCustomerId(Integer customerId) {
//        return orderRepository.findByCustomerId(customerId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Order> getOrdersByStatus(String status) {
//        return orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()));
//    }
//
//}