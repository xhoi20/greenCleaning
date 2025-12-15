package com.laundry.controller;


import com.laundry.dto.EmployeeRegistrationRequest;
import com.laundry.dto.EmployeeUpdateRequest;
import com.laundry.entity.Employee;
import com.laundry.entity.EmployeeRole;
import com.laundry.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController  // Për JSON responses, jo views
@RequestMapping("/api/employees")  // Base path: /api/employees
public class EMPLOYEE {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> registerEmployee(@RequestBody EmployeeRegistrationRequest request) {
        return employeeService.registerEmployee(request);
    }

    // Update (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Integer id,
                                            @RequestBody EmployeeUpdateRequest updateRequest,
                                            @RequestParam(defaultValue = "ADMIN") EmployeeRole requestingUserRole) {
        // requestingUserRole si param për të plotësuar thirrjen në service (mund ta marrësh nga token në prodhim)
        return employeeService.updateEmployee(id, updateRequest, requestingUserRole);
    }

    // Merr një employee me ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        return ResponseEntity.of(employeeService.getEmployeeById(id));
    }

    // Merr të gjithë employees (GET)
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // Fshi me ID (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployeeById(@PathVariable Integer id) {
        try {
            employeeService.deleteEmployeeById(id);
            return ResponseEntity.ok().build();  // 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Merr employees sipas rolit (GET)
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getEmployeesByRole(@PathVariable EmployeeRole role) {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(role));
    }
}