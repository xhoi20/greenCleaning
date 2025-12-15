package com.laundry.service.serviceInterface;


import com.laundry.dto.CustomerRegistrationRequest;
import com.laundry.dto.CustomerUpdateRequest;
import com.laundry.entity.Customer;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICustomerService {
    ResponseEntity<Map<String, Object>> registerCustomer(CustomerRegistrationRequest request);
    ResponseEntity<Map<String, Object>> updateCustomer(Integer id,  CustomerUpdateRequest updateRequest);
    Optional<Customer> getCustomerById(Integer id);
    Iterable<Customer> getAllCustomers();
    void deleteCustomerById(Integer id);
    Optional<Customer> findByPhone(String phone);
    List<Customer> getActiveCustomers(int days);
}