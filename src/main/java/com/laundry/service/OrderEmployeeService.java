package com.laundry.service;


import com.laundry.entity.*;
import com.laundry.dto.OrderEmployeeCreateDTO; // Asumo DTO me orderId, employeeId, assignedRole
import com.laundry.dto.OrderEmployeeUpdateDTO; // Asumo DTO me assignedRole (për update)
import com.laundry.repository.OrderEmployeeRepository; // Asumo ekziston me extends JpaRepository
import com.laundry.service.OrderService;
import com.laundry.service.EmployeeService;
import com.laundry.service.serviceInterface.IOrderEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderEmployeeService extends BaseService implements IOrderEmployeeService {

    private final OrderEmployeeRepository orderEmployeeRepository;

    @Autowired
    @Lazy
    private OrderService orderService;

    @Autowired
    @Lazy
    private EmployeeService employeeService;

    public OrderEmployeeService(OrderEmployeeRepository orderEmployeeRepository) {
        this.orderEmployeeRepository = orderEmployeeRepository;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createOrderEmployee(OrderEmployeeCreateDTO request) {
        try {
            if (request.getOrderId() == null) {
                return createErrorResponse("Order ID is missing", HttpStatus.BAD_REQUEST);
            }
            if (request.getEmployeeId() == null) {
                return createErrorResponse("Employee ID is missing", HttpStatus.BAD_REQUEST);
            }
            if (request.getAssignedRole() == null) {
                return createErrorResponse("Assigned role is missing", HttpStatus.BAD_REQUEST);
            }

            Optional<Order> orderOpt = orderService.getOrderById(request.getOrderId());
            if (orderOpt.isEmpty()) {
                return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
            }

            Optional<Employee> employeeOpt = employeeService.getEmployeeById(request.getEmployeeId());
            if (employeeOpt.isEmpty()) {
                return createErrorResponse("Employee not found", HttpStatus.NOT_FOUND);
            }
            getAuthenticatedEmployee();

          //  getAuthenticatedUser();

            OrderEmployee newAssignment = new OrderEmployee();
            newAssignment.setOrder(orderOpt.get());
            newAssignment.setEmployee(employeeOpt.get());
            newAssignment.setAssignedRole(request.getAssignedRole());
            newAssignment.setAssignedAt(LocalDateTime.now());

            OrderEmployee savedAssignment = orderEmployeeRepository.save(newAssignment);
            return createSuccessResponse(savedAssignment, "Employee assigned successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> updateOrderEmployee(Integer id, OrderEmployeeUpdateDTO updateRequest) {
        try {
            getAuthenticatedUser();
            Optional<OrderEmployee> assignmentOpt = orderEmployeeRepository.findById(id);
            if (assignmentOpt.isPresent()) {
                OrderEmployee assignment = assignmentOpt.get();
                if (updateRequest.getAssignedRole() != null) {
                    assignment.setAssignedRole(updateRequest.getAssignedRole());
                }
                return createSuccessResponse(orderEmployeeRepository.save(assignment), "Assignment updated successfully", HttpStatus.OK);
            }
            return createErrorResponse("Assignment not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<OrderEmployee> getOrderEmployeeById(Integer id) {
        return orderEmployeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Iterable<OrderEmployee> getAllOrderEmployees() {
        return orderEmployeeRepository.findAll();
    }

    @Transactional
    public void deleteOrderEmployeeById(Integer id) {
        getAuthenticatedUser();
        if (!orderEmployeeRepository.existsById(id)) {
            throw new RuntimeException("Assignment with ID " + id + " not found.");
        }
        orderEmployeeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderEmployee> getByOrderId(Integer orderId) {
        return orderEmployeeRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderEmployee> getByEmployeeId(Integer employeeId) {
        return orderEmployeeRepository.findByEmployeeId(employeeId);
    }
}