package com.laundry.service.serviceInterface;

import com.laundry.dto.PaymentCreateDTO;
import com.laundry.dto.PaymentUpdateDTO;
import com.laundry.entity.Payment;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPaymentService {
    ResponseEntity<Map<String, Object>> createPayment(PaymentCreateDTO request);
    ResponseEntity<Map<String, Object>> updatePayment(Integer id, PaymentUpdateDTO updateRequest);
    Optional<Payment> getPaymentById(Integer id);
    Iterable<Payment> getAllPayments();
    void deletePaymentById(Integer id);
    List<Payment> getPaymentsByOrderId(Integer orderId);
}