package com.laundry.service;

import com.laundry.dto.AuthRequest;
import com.laundry.dto.AuthResponse;
import com.laundry.dto.EmployeeRegistrationRequest;
import com.laundry.dto.EmployeeUpdateRequest;
import com.laundry.entity.Employee;
import com.laundry.entity.EmployeeRole;
import com.laundry.repository.EmployeeRepository;
import com.laundry.service.serviceInterface.IEmployeeService;
import com.laundry.tokenlogin.JwtUtil;
import jakarta.transaction.Transactional;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeService extends BaseService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    @Value("${jasypt.encryptor.password}")
    private String encryptionKey;

    private static final String ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES";
    @Autowired
    private JwtUtil jwtUtil;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> registerEmployee(EmployeeRegistrationRequest request) {
        try {
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return createErrorResponse("First name is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return createErrorResponse("Last name is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getRole() == null || request.getRole().trim().isEmpty()) {
                return createErrorResponse("Role cannot be null or empty", HttpStatus.BAD_REQUEST);
            }

            EmployeeRole role;
            try {
                role = EmployeeRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return createErrorResponse("Invalid role provided", HttpStatus.BAD_REQUEST);
            }

          getAuthenticatedUser();

            Employee employee = new Employee();
            employee.setFirstName(request.getFirstName());
            employee.setLastName(request.getLastName());
            employee.setRole(role);
//            employee.setPassword(request.getPassword());
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
                encryptor.setPassword(encryptionKey);
                encryptor.setAlgorithm(ENCRYPTION_ALGORITHM);
                String encryptedPassword = encryptor.encrypt(request.getPassword());
                employee.setPassword(encryptedPassword);
            } else if (employee.getId() == null) {
                throw new IllegalArgumentException("Password cannot be set empty for new users");
            }
                employee.setPhone(request.getPhone());
                employee.setSalary(request.getSalary());
                employee.setHireDate(request.getHireDate());
                employee.setCreatedAt(LocalDateTime.now());
                employee.setIsActive(true);

                return createSuccessResponse(employeeRepository.save(employee), "Employee registered successfully", HttpStatus.CREATED);
            } catch(Exception e){
                return handleException(e);
            }
        }

    @Transactional
    public AuthResponse loginEmployee(AuthRequest authRequest) {
        Employee employee = employeeRepository.findByFirstName(authRequest.getFirstName());
        if (employee == null) {
            throw new RuntimeException("User not found");
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionKey);
        encryptor.setAlgorithm(ENCRYPTION_ALGORITHM);
        String decryptedPassword = encryptor.decrypt( employee.getPassword());
        if (!decryptedPassword.equals(authRequest.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }


        if ( employee.getRole()!=EmployeeRole.ADMIN && employee.getRole()!=EmployeeRole.CLEANER&& employee.getRole() != EmployeeRole.DELIVERY) {
            throw new IllegalArgumentException("User must be  employ to log in");
        }

        String token = jwtUtil.generateToken(employee.getFirstName(), employee.getRole().name());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setFirstName(employee.getFirstName());
        response.setRole(employee.getRole().name());
        return response;
    }
    @Transactional
    public ResponseEntity<Map<String, Object>> updateEmployee(Integer id, EmployeeUpdateRequest updateRequest, EmployeeRole requestingUserRole) {
        try {
            if (updateRequest.getFirstName() == null || updateRequest.getFirstName().trim().isEmpty()) {
                return createErrorResponse("First name cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (updateRequest.getLastName() == null || updateRequest.getLastName().trim().isEmpty()) {
                return createErrorResponse("Last name cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (updateRequest.getRole() == null || updateRequest.getRole().trim().isEmpty()) {
                return createErrorResponse("Role cannot be empty", HttpStatus.BAD_REQUEST);
            }

            EmployeeRole role;
            try {
                role = EmployeeRole.valueOf(updateRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return createErrorResponse("Invalid role provided", HttpStatus.BAD_REQUEST);
            }

            getAuthenticatedUser();

            Optional<Employee> employeeOptional = employeeRepository.findById(id);
            if (employeeOptional.isPresent()) {
                Employee employee = employeeOptional.get();
                employee.setFirstName(updateRequest.getFirstName());
                employee.setLastName(updateRequest.getLastName());
                employee.setRole(role);
                employee.setPhone(updateRequest.getPhone());
                employee.setSalary(updateRequest.getSalary());
                employee.setHireDate(updateRequest.getHireDate());
                employee.setIsActive(updateRequest.isActive());

                return createSuccessResponse(employeeRepository.save(employee), "Employee updated successfully", HttpStatus.OK);
            } else {
                return createErrorResponse("Employee not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Optional<Employee> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public Iterable<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional
    public void deleteEmployeeById(Integer id) {
        getAuthenticatedUser();
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee with ID " + id + " not found.");
        }
        employeeRepository.deleteById(id);
    }


    public List<Employee> getEmployeesByRole(EmployeeRole role) {
        return employeeRepository.findByRole(role);
    }
    public Optional<Employee> getEmployeeByName(String firstName) {
        return Optional.ofNullable(employeeRepository.findByFirstName(firstName));
    }
}