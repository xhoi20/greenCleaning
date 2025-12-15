package com.laundry.service.serviceInterface;


import com.laundry.dto.EmployeeRegistrationRequest;
import com.laundry.dto.EmployeeUpdateRequest;
import com.laundry.entity.Employee;
import com.laundry.entity.EmployeeRole;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IEmployeeService {
    ResponseEntity<Map<String, Object>> registerEmployee(EmployeeRegistrationRequest request);
    ResponseEntity<Map<String, Object>> updateEmployee(Integer id, EmployeeUpdateRequest updateRequest, EmployeeRole requestingUserRole);
    Optional<Employee> getEmployeeById(Integer id);
    Iterable<Employee> getAllEmployees();
    void deleteEmployeeById(Integer id);
    Optional<Employee> getEmployeeByName(String firstName);
    List<Employee> getEmployeesByRole(EmployeeRole role);
}