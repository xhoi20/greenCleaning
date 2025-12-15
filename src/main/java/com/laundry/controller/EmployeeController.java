package com.laundry.controller;

import com.laundry.dto.EmployeeRegistrationRequest;
import com.laundry.dto.EmployeeUpdateRequest;
import com.laundry.entity.Employee;
import com.laundry.entity.EmployeeRole;
import com.laundry.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/employee-list")
    public String getAllEmployees(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String firstName = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("");
            model.addAttribute("firstName", firstName);
            model.addAttribute("role", role);
            Iterable<Employee> employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
        }
        return "employee-list";
    }

    @RequestMapping(value = "/employee-form", method = {RequestMethod.GET, RequestMethod.POST})
    public String handleEmployeeForm(@ModelAttribute("employeeRegistrationRequest") @Valid EmployeeRegistrationRequest employeeRegistrationRequest,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model,
                                     HttpServletRequest request) {
        if (request.getMethod().equalsIgnoreCase("GET")) {
            model.addAttribute("employeeRegistrationRequest", new EmployeeRegistrationRequest());
            model.addAttribute("roles", EmployeeRole.values());
            return "employee-form";
        }

        model.addAttribute("roles", EmployeeRole.values());

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("employeeRegistrationRequest", employeeRegistrationRequest);
            return "employee-form";
        }

        ResponseEntity<Map<String, Object>> response = employeeService.registerEmployee(employeeRegistrationRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Employee saved successfully!");
            return "redirect:/employees/employee-list";
        } else {
            model.addAttribute("employeeRegistrationRequest", employeeRegistrationRequest); // Shto për error nga service
            model.addAttribute("errorMessage", response.getBody().get("message"));
            return "employee-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditEmployeeForm(@PathVariable Integer id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Punonjësi nuk u gjet"));


        EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setFirstName(employee.getFirstName());
        updateRequest.setLastName(employee.getLastName());
        updateRequest.setRole(employee.getRole().name());
        updateRequest.setPhone(employee.getPhone());
   updateRequest.setSalary(employee.getSalary());

        updateRequest.setHireDate(employee.getHireDate());
        updateRequest.setActive(employee.getIsActive());

        model.addAttribute("employeeUpdateRequest", updateRequest);
        model.addAttribute("roles", EmployeeRole.values());
        return "edit-employee";
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Integer id,
                                 @Valid @ModelAttribute("employeeUpdateRequest") EmployeeUpdateRequest updateRequest, // Ndryshoje në DTO + emër i saktë
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("employeeUpdateRequest", updateRequest);
            model.addAttribute("roles", EmployeeRole.values());
            return "edit-employee";
        }

        try {
            ResponseEntity<Map<String, Object>> response = employeeService.updateEmployee(id, updateRequest, EmployeeRole.ADMIN);
            if (response.getStatusCode() == HttpStatus.OK) {
                redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
                return "redirect:/employees/employee-list";
            } else {
                model.addAttribute("employeeUpdateRequest", updateRequest); // Shto për error nga service
                model.addAttribute("roles", EmployeeRole.values());
                redirectAttributes.addFlashAttribute("errorMessage", response.getBody().get("message"));
                return "redirect:/employees/edit/" + id;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update employee: " + e.getMessage());
            return "redirect:/employees/edit/" + id;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred");
            return "redirect:/employees/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployeeById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employees/employee-list";
    }
}