package com.laundry.controller;



import com.laundry.dto.CustomerRegistrationRequest;
import com.laundry.dto.CustomerUpdateRequest;
import com.laundry.entity.Customer;
import com.laundry.entity.Order;
import com.laundry.service.CustomerService;
import com.laundry.service.OrderService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderService orderService;
    // 1. Listë e të gjithë klientëve (përdor getAllCustomers)
    @GetMapping("/customer-list")
    public String getAllCustomers(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String firstName = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("");
            model.addAttribute("firstName", firstName);
            model.addAttribute("role", role);
        }
        Iterable<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customer-list";
    }


    @GetMapping("/active-customers")
    public String getActiveCustomers(@RequestParam(defaultValue = "30") int days, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String firstName = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("");
            model.addAttribute("firstName", firstName);
            model.addAttribute("role", role);
        }
        List<Customer> activeCustomers = customerService.getActiveCustomers(days);
        model.addAttribute("customers", activeCustomers);
        model.addAttribute("days", days);
        model.addAttribute("pageTitle", "Active Customers (Last " + days + " Days)");
        return "customer-list";
    }

    // 3. Formë për Create (përdor registerCustomer)
    @RequestMapping(value = "/customer-form", method = {RequestMethod.GET, RequestMethod.POST})
    public String handleCustomerForm(@ModelAttribute("customerRegistrationRequest") @Valid CustomerRegistrationRequest customerRegistrationRequest,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model,
                                     HttpServletRequest request) {
        if (request.getMethod().equalsIgnoreCase("GET")) {
            model.addAttribute("customerRegistrationRequest", new CustomerRegistrationRequest());
            return "customer-form";
        }

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("customerRegistrationRequest", customerRegistrationRequest);
            return "customer-form";
        }

        ResponseEntity<Map<String, Object>> response = customerService.registerCustomer(customerRegistrationRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Customer saved successfully!");
            return "redirect:/customers/customer-list";
        } else {
            model.addAttribute("customerRegistrationRequest", customerRegistrationRequest);
            model.addAttribute("errorMessage", response.getBody().get("message"));
            return "customer-form";
        }
    }

    // 4. Formë për Edit (përdor updateCustomer)
    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Integer id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Klienti nuk u gjet"));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setFirstName(customer.getFirstName());
        updateRequest.setLastName(customer.getLastName());
        updateRequest.setPhone(customer.getPhone());
        updateRequest.setEmail(customer.getEmail());
        updateRequest.setAddress(customer.getAddress());

        model.addAttribute("customerUpdateRequest", updateRequest);
        return "edit-customer";
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Integer id,
                                 @Valid @ModelAttribute("customerUpdateRequest") CustomerUpdateRequest updateRequest,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("customerUpdateRequest", updateRequest);
            return "edit-customer";
        }

        try {
            ResponseEntity<Map<String, Object>> response = customerService.updateCustomer(id,  updateRequest);
            if (response.getStatusCode() == HttpStatus.OK) {
                redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
                return "redirect:/customers/customer-list";
            } else {
                model.addAttribute("customerUpdateRequest", updateRequest);
                model.addAttribute("errorMessage", response.getBody().get("message"));
                return "edit-customer";
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update customer: " + e.getMessage());
            return "redirect:/customers/edit/" + id;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred");
            return "redirect:/customers/edit/" + id;
        }
    }

    // 5. Delete (përdor deleteCustomerById)
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomerById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers/customer-list";
    }
    // 7. Detaje të klientit me ID (p.sh., për view)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String firstName = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("");
            model.addAttribute("firstName", firstName);
            model.addAttribute("role", role);
        }
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            model.addAttribute("customer", customer);
            model.addAttribute("detailTitle", "Detajet e klientit: #" + id);
            List<Order> orders = orderService.getOrdersByCustomerId(id);
            model.addAttribute("orders", orders);
            return "customer-detail";
        } else {
            model.addAttribute("errorMessage", "Klienti nuk u gjet me ID: " + id);
            return "redirect:/customers/customer-list";
        }
    }

    @GetMapping("/search-by-phone")
    public String searchByPhone(@RequestParam String phone, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String firstName = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse("");
            model.addAttribute("firstName", firstName);
            model.addAttribute("role", role);
        }
        Optional<Customer> customer = customerService.findByPhone(phone);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            model.addAttribute("searchResult", "Customer found!");
            List<Order> orders = orderService.getOrdersByCustomerId(customer.get().getId());
            model.addAttribute("orders", orders);
        } else {
            model.addAttribute("searchResult", "No customer found with phone: " + phone);
        }
        return "customer-detail";
    }
}