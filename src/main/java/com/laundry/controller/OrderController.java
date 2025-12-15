
package com.laundry.controller;
import com.laundry.entity.OrderEmployee;
import com.laundry.entity.Employee;
//import com.laundry.entity.OrderService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.laundry.dto.OrderCreateDTO;
import com.laundry.dto.OrderUpdateDTO;
import com.laundry.dto.PaymentCreateDTO;
import com.laundry.entity.Employee;
import com.laundry.entity.Order;

import com.laundry.service.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ServiceService serviceService;  // SHTUAR: Autowired për ServiceService

    // 1. Listë e të gjithë porosisë (përdor getAllOrders)
    @GetMapping
    @Transactional(readOnly = true)
    public String getAllOrders(Model model, Authentication authentication) {
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
        Iterable<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "order-list";  // Template: order-list.html
    }

    // Opsionale: Listë sipas statusit ose customer (p.sh., me parametra)
    @GetMapping("/by-status")
    @Transactional(readOnly = true)
    public String getOrdersByStatus(@RequestParam String status, Model model, Authentication authentication) {
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
        List<Order> orders = orderService.getOrdersByStatus(status.toUpperCase());
        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", status);
        return "order-list";  // E njëjta template, me filter
    }

    @GetMapping("/by-customer")
    @Transactional(readOnly = true)
    public String getOrdersByCustomer(@RequestParam Integer customerId, Model model, Authentication authentication) {
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
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        model.addAttribute("orders", orders);
        model.addAttribute("customerIdFilter", customerId);
        return "order-list";  // E njëjta template
    }

    @GetMapping("/add")
    public String showCreateOrderForm(Model model, Authentication authentication) {
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
        model.addAttribute("orderCreateDTO", new OrderCreateDTO());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("services", serviceService.getAllServices());
       // model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("initialItemCount", 0);
        model.addAttribute("initialServiceCount", 0);  // SHTUAR: Për të menaxhuar numrin fillestar të services në JS/form

        return "order-form";
    }


    // 3. Create (POST) - Përdor service-in
    @PostMapping("/add")
    public String createOrder(@Valid @ModelAttribute("orderCreateDTO") OrderCreateDTO orderCreateDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model,
                              Authentication authentication) {
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

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("orderCreateDTO", orderCreateDTO);

            // SHTO EDHE KËTË NË POST NËSE KA GABIME (për të mbajtur itemCounter në rikthim)
            model.addAttribute("initialItemCount", orderCreateDTO.getOrderItems() != null ? orderCreateDTO.getOrderItems().size() : 0);
            model.addAttribute("initialServiceCount", orderCreateDTO.getOrderServices() != null ? orderCreateDTO.getOrderServices().size() : 0);  // SHTUAR: Për services
            model.addAttribute("customers", customerService.getAllCustomers());  // Rikthe customers nëse ka error
            model.addAttribute("services", serviceService.getAllServices());  // SHTUAR: Rikthe services nëse ka error

            return "order-form";  // Rikthe te form nëse ka gabime
        }

        ResponseEntity<Map<String, Object>> response = orderService.createOrder(orderCreateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {

            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully!");
            return "redirect:/orders";  // Redirect te lista
        } else {
            model.addAttribute("orderCreateDTO", orderCreateDTO);
            model.addAttribute("errorMessage", response.getBody().get("error"));  // Ose "message" nga service

            // SHTO EDHE KËTË NËSE KA ERROR NGA SERVICE (për të mbajtur itemCounter)
            model.addAttribute("initialItemCount", orderCreateDTO.getOrderItems() != null ? orderCreateDTO.getOrderItems().size() : 0);
            model.addAttribute("initialServiceCount", orderCreateDTO.getOrderServices() != null ? orderCreateDTO.getOrderServices().size() : 0);  // SHTUAR: Për services
            model.addAttribute("customers", customerService.getAllCustomers());  // Rikthe customers
            model.addAttribute("services", serviceService.getAllServices());  // SHTUAR: Rikthe services
          //  model.addAttribute("employees", employeeService.getAllEmployees());
            return "order-form";
        }
    }

    // 4. Formë për Edit (GET) - Përdor update DTO
    @GetMapping("/edit/{id}")
    public String showEditOrderForm(@PathVariable Integer id, Model model, Authentication authentication) {
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

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            OrderUpdateDTO updateDTO = OrderUpdateDTO.builder()
                    .id(order.getId())
                    .pickupDate(order.getPickupDate())
                    .totalAmount(order.getTotalAmount())
                    .notes(order.getNotes())
                    .status(order.getStatus().name())  // Konverto enum në string
                    .build();
            model.addAttribute("orderUpdateDTO", updateDTO);
            model.addAttribute("orderId", id);
            return "edit-order";  // E njëjta template si create, ose "edit-order.html"
        } else {
            model.addAttribute("errorMessage", "Order nuk u gjet me ID: " + id);
            return "redirect:/orders";
        }
    }

    // 5. Update (POST) - Përdor service-in
    @PostMapping("/edit/{id}")
    public String updateOrder(@PathVariable Integer id,
                              @Valid @ModelAttribute("orderUpdateDTO") OrderUpdateDTO orderUpdateDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model,
                              Authentication authentication) {
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

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("orderUpdateDTO", orderUpdateDTO);
            model.addAttribute("orderId", id);
            return "edit-order";
        }

        ResponseEntity<Map<String, Object>> response = orderService.updateOrder(id, orderUpdateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully!");
            return "redirect:/orders";
        } else {
            model.addAttribute("orderUpdateDTO", orderUpdateDTO);
            model.addAttribute("orderId", id);
            model.addAttribute("errorMessage", response.getBody().get("error"));
            return "edit-order";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrderById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    // 7. Detaje të porosisë me ID (p.sh., për view)
    @GetMapping("/detail/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Integer id, Model model, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
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
      //  Optional<Order> orderOpt = orderService.getOrderById(id);
        Optional<Order> orderOpt = orderService.getOrderWithDetails(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // lista e employees të lidhur me këtë order
            List<Employee> employees = order.getOrderEmployees()
                    .stream()
                    .map(OrderEmployee::getEmployee)
                    .toList();

            // lista e shërbimeve (OrderService) nga të gjithë item-ët
            List<com.laundry.entity.OrderService> orderServices = order.getOrderItems()
                    .stream()
                    .flatMap(oi -> oi.getOrderServices().stream())
                    .toList();

            model.addAttribute("order", order);
            model.addAttribute("orderItems", order.getOrderItems());
            model.addAttribute("payments", order.getPayments());
            model.addAttribute("employees", employees);          // 👈 emrat e employee
            model.addAttribute("orderServices", orderServices);  // 👈 lista e OrderService
            model.addAttribute("detailTitle", "Detajet e porosisë: #" + id);

            return "order-detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order nuk u gjet me ID: " + id);
            return "redirect:/orders";
        }

//        if (orderOpt.isPresent()) {
//            model.addAttribute("order", orderOpt.get());
//            model.addAttribute("detailTitle", "Detajet e porosisë: #" + id);
//            // Opsionale: Shto orderItems, payments në model nëse dëshiron të shfaqësh
//            return "order-detail";
//        } else {
//            model.addAttribute("errorMessage", "Order nuk u gjet me ID: " + id);
//            return "redirect:/orders";
//        }
    }
}
