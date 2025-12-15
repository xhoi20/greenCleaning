package com.laundry.service;

import com.laundry.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import com.laundry.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.laundry.entity.EmployeeRole.ADMIN;

@Service
public abstract class BaseService {

    @Autowired
    protected EmployeeRepository employeeRepository;

    protected Employee getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }

        String firstname = authentication.getName();
        Employee employeeOpt = employeeRepository.findByFirstName(firstname);
        if (employeeOpt == null) {
            throw new UsernameNotFoundException("Employee not found: " + firstname);  // Check manual null
        }
        Employee employee = employeeOpt;
        if (!employee.getRole().equals(ADMIN)) {
            throw new SecurityException("Only an admin can perform this operation.");
        }

        return employee;
    }

    protected <T> ResponseEntity<Map<String, Object>> createSuccessResponse(T data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }

    protected ResponseEntity<Map<String, Object>> createErrorResponse(String error, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        return new ResponseEntity<>(response, status);
    }

    protected ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        if (e instanceof SecurityException || e instanceof UsernameNotFoundException) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } else if (e instanceof IllegalArgumentException) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            System.out.println("Unexpected exception: " + e.getMessage());
            response.put("error", "An unexpected error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
