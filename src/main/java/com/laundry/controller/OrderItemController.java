package com.laundry.controller;


import com.laundry.dto.OrderItemCreateDTO;
import com.laundry.dto.OrderItemUpdateDTO;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.service.OrderItemService;
import com.laundry.service.OrderService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    // 1. Listë e të gjithë order items (përdor getAllOrderItems)
    @GetMapping
    @Transactional(readOnly = true)
    public String getAllOrderItems(Model model, Authentication authentication) {
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
        Iterable<OrderItem> orderItems = orderItemService.getAllOrderItems();
        model.addAttribute("orderItems", orderItems);
        return "orderitem-list";  // Template: orderitem-list.html
    }

    // Opsionale: Listë sipas order ID
    @GetMapping("/by-order/{orderId}")
    @Transactional(readOnly = true)
    public String getOrderItemsByOrderId(@PathVariable Integer orderId, Model model, Authentication authentication) {
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
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderIdFilter", orderId);
        return "orderitem-list";  // E njëjta template, me filter
    }

    // Opsionale: Kërko sipas tag ID
    @GetMapping("/by-tag/{tagId}")
    @Transactional(readOnly = true)
    public String getOrderItemByTagId(@PathVariable String tagId, Model model, Authentication authentication) {
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
        Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemByTagId(tagId);
        if (orderItemOpt.isPresent()) {
            model.addAttribute("orderItem", orderItemOpt.get());
            model.addAttribute("tagIdFilter", tagId);
            return "orderitem-detail";  // Ose "orderitem-list" nëse dëshiron të shfaqësh si listë me një item
        } else {
            model.addAttribute("errorMessage", "OrderItem nuk u gjet me tag ID: " + tagId);
            return "redirect:/order-items";
        }
    }

    // 2. Formë për Create (GET) - Përdor create DTO, ngarko orders për selektim
    @GetMapping("/add")
    public String showCreateOrderItemForm(Model model, Authentication authentication) {
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
        model.addAttribute("orderItemCreateDTO", new OrderItemCreateDTO());
        Iterable<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orderitem-form";
    }

    // 3. Create (POST) - Përdor service-in
    @PostMapping("/add")
    public String createOrderItem(@Valid @ModelAttribute("orderItemCreateDTO") OrderItemCreateDTO orderItemCreateDTO,
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
            model.addAttribute("orderItemCreateDTO", orderItemCreateDTO);
            Iterable<Order> orders = orderService.getAllOrders();
            model.addAttribute("orders", orders);
            return "orderitem-form";  // Rikthe te form nëse ka gabime
        }

        ResponseEntity<Map<String, Object>> response = orderItemService.createOrderItem(orderItemCreateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "OrderItem created successfully!");
            return "redirect:/order-items";  // Redirect te lista
        } else {
            model.addAttribute("orderItemCreateDTO", orderItemCreateDTO);
            Iterable<Order> orders = orderService.getAllOrders();
            model.addAttribute("orders", orders);
            model.addAttribute("errorMessage", response.getBody().get("error"));  // Ose "message" nga service
            return "orderitem-form";
        }
    }

    // 4. Formë për Edit (GET) - Përdor update DTO
    @GetMapping("/edit/{id}")
    public String showEditOrderItemForm(@PathVariable Integer id, Model model, Authentication authentication) {
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

        Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(id);
        if (orderItemOpt.isPresent()) {
            OrderItem orderItem = orderItemOpt.get();
            OrderItemUpdateDTO updateDTO = OrderItemUpdateDTO.builder()
                    .id(orderItem.getId())
                    .itemDescription(orderItem.getItemDescription())
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .build();
            model.addAttribute("orderItemUpdateDTO", updateDTO);
            model.addAttribute("orderItemId", id);
            return "edit-orderitem";  // E njëjta template si create, ose "orderitem-edit.html"
        } else {
            model.addAttribute("errorMessage", "OrderItem nuk u gjet me ID: " + id);
            return "redirect:/order-items";
        }
    }

    // 5. Update (POST) - Përdor service-in
    @PostMapping("/edit/{id}")
    public String updateOrderItem(@PathVariable Integer id,
                                  @Valid @ModelAttribute("orderItemUpdateDTO") OrderItemUpdateDTO orderItemUpdateDTO,
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
            model.addAttribute("orderItemUpdateDTO", orderItemUpdateDTO);
            model.addAttribute("orderItemId", id);
            return "edit-orderitem";
        }

        ResponseEntity<Map<String, Object>> response = orderItemService.updateOrderItem(id, orderItemUpdateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "OrderItem updated successfully!");
            return "redirect:/order-items";
        } else {
            model.addAttribute("orderItemUpdateDTO", orderItemUpdateDTO);
            model.addAttribute("orderItemId", id);
            model.addAttribute("errorMessage", response.getBody().get("error"));
            return "edit-orderitem";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteOrderItem(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderItemService.deleteOrderItemById(id);
            redirectAttributes.addFlashAttribute("successMessage", "OrderItem deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order-items";
    }


    }

