package com.laundry.service;

import com.laundry.dto.PaymentCreateDTO;
import com.laundry.dto.PaymentUpdateDTO;
import com.laundry.entity.EmployeeRole;
import com.laundry.entity.Payment;
import com.laundry.entity.Order;
import com.laundry.entity.PaymentStatus;
import com.laundry.repository.PaymentRepository;
import com.laundry.service.serviceInterface.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService extends BaseService implements IPaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    @Lazy
    private OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


@Transactional
public ResponseEntity<Map<String, Object>> createPayment(PaymentCreateDTO request) {
    try {
        if (request.getOrderId() == null) {
            return createErrorResponse("Order ID is missing", HttpStatus.BAD_REQUEST);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createErrorResponse("Invalid amount", HttpStatus.BAD_REQUEST);
        }
        if (request.getPaymentMethod() == null) {
            return createErrorResponse("Payment method is missing", HttpStatus.BAD_REQUEST);
        }

        Optional<Order> orderOpt = orderService.getOrderById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
        }
        getAuthenticatedEmployee();

       //getAuthenticatedUser();

        Payment newPayment = Payment.builder()
                .order(orderOpt.get())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(newPayment);
        return createSuccessResponse(savedPayment, "Payment created successfully", HttpStatus.CREATED);
    } catch (Exception e) {
        return handleException(e);
    }
}

    @Transactional
    public ResponseEntity<Map<String, Object>> updatePayment(Integer id, PaymentUpdateDTO updateRequest) {
        try {
            getAuthenticatedUser();
            Optional<Payment> paymentOpt = paymentRepository.findById(id);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (updateRequest.getAmount() != null && updateRequest.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    payment.setAmount(updateRequest.getAmount());
                }
                if (updateRequest.getPaymentMethod() != null) {
                    payment.setPaymentMethod(updateRequest.getPaymentMethod());
                }
                if (updateRequest.getStatus() != null) {
                    payment.setStatus(updateRequest.getStatus());
                }
                return createSuccessResponse(paymentRepository.save(payment), "Payment updated successfully", HttpStatus.OK);
            }
            return createErrorResponse("Payment not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(Integer id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Iterable<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public void deletePaymentById(Integer id) {
        getAuthenticatedUser();
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment with ID " + id + " not found.");
        }
        paymentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(Integer orderId) {
        return paymentRepository.findPaymentsByOrderId(orderId);
    }
}