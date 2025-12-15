package com.laundry.service;

import com.laundry.dto.CustomerRegistrationRequest;
import com.laundry.dto.CustomerUpdateRequest;
import com.laundry.entity.Customer;
import com.laundry.repository.CustomerRepository;
import com.laundry.service.serviceInterface.ICustomerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerService extends BaseService implements ICustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Transactional
    public ResponseEntity<Map<String, Object>> registerCustomer(CustomerRegistrationRequest request) {
        try {
            // Validime bazë (siç ke, por përdor 'request')
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return createErrorResponse("First name is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return createErrorResponse("Last name is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                return createErrorResponse("Phone is missing or empty", HttpStatus.BAD_REQUEST);
            }

            // Kontrollo duplicate phone
            Optional<Customer> existingCustomer = customerRepository.findByPhone(request.getPhone());
            if (existingCustomer.isPresent()) {
                return createErrorResponse("Customer with this phone already exists", HttpStatus.BAD_REQUEST);
            }
            getAuthenticatedUser();
            // Krijo entity të re nga DTO DHE set-o timestamps KËTU
            Customer newCustomer = new Customer();  // <- Entity e re
            newCustomer.setFirstName(request.getFirstName());
            newCustomer.setLastName(request.getLastName());
            newCustomer.setPhone(request.getPhone());
            newCustomer.setEmail(request.getEmail());
            newCustomer.setAddress(request.getAddress());
            newCustomer.setCreatedAt(LocalDateTime.now());  // <- Vetëm në entity
            newCustomer.setUpdatedAt(LocalDateTime.now());  // <- Vetëm në entity

            // Ruaj entity-n
            Customer savedCustomer = customerRepository.save(newCustomer);

            return createSuccessResponse(savedCustomer, "Customer registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Për update, ngjashëm (nëse nuk e ke rregulluar tashmë)
    @Transactional
    public ResponseEntity<Map<String, Object>> updateCustomer(Integer id, CustomerUpdateRequest updateRequest) {
        try {
            // Validime...
            getAuthenticatedUser();
            Optional<Customer> customerOptional = customerRepository.findById(id);
            if (customerOptional.isPresent()) {
                Customer customer = customerOptional.get();
                customer.setFirstName(updateRequest.getFirstName());
                customer.setLastName(updateRequest.getLastName());
                customer.setPhone(updateRequest.getPhone());
                customer.setEmail(updateRequest.getEmail());
                customer.setAddress(updateRequest.getAddress());
                customer.setUpdatedAt(LocalDateTime.now());  // Vetëm updatedAt në entity

                return createSuccessResponse(customerRepository.save(customer), "Customer updated successfully", HttpStatus.OK);
            } else {
                return createErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }
    @Transactional
    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }

    @Transactional
    public Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void deleteCustomerById(Integer id) {
        getAuthenticatedUser();
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer with ID " + id + " not found.");
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    @Transactional
    public List<Customer> getActiveCustomers(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return customerRepository.findByOrdersCreatedAfter(threshold);

    }
}