package com.laundry.controller;


import com.laundry.dto.OrderServiceCreateDTO;
import com.laundry.dto.OrderServiceUpdateDTO;
import com.laundry.entity.OrderItem;
import com.laundry.entity.OrderService;
import com.laundry.entity.Services;
import com.laundry.service.OrderItemService;
import com.laundry.service.OrderServiceService;
import com.laundry.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/order-services")
public class OrderServiceController {

    @Autowired
    private OrderServiceService orderServiceService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ServiceService serviceService;

    // 1. Lista e të gjitha OrderService-ve (të lidhura me OrderItem)
    @GetMapping
    public String getAllOrderServices(Model model, Authentication authentication) {
        setCommonAttributes(model, authentication);
        Iterable<OrderService> orderServices = orderServiceService.getAllOrderServices();
        model.addAttribute("orderServices", orderServices);
        return "orderservice-list"; // templates/orderservice-list.html
    }

    // 2. Lista e OrderService-ve për një OrderItem specifik
    @GetMapping("/by-order-item/{orderItemId}")
    public String getByOrderItemId(@PathVariable Integer orderItemId,
                                   Model model,
                                   Authentication authentication) {
        setCommonAttributes(model, authentication);

        Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(orderItemId);
        if (orderItemOpt.isEmpty()) {
            model.addAttribute("errorMessage", "OrderItem me ID " + orderItemId + " nuk u gjet.");
            return "redirect:/order-services";
        }

        List<OrderService> services = orderServiceService.getByOrderItemId(orderItemId);
        model.addAttribute("orderServices", services);
        model.addAttribute("orderItem", orderItemOpt.get());
        model.addAttribute("orderItemIdFilter", orderItemId);

        return "orderservice-list";
    }

    // 3. Forma për të shtuar shërbim të ri (GET)
    @GetMapping("/add/{orderItemId}")
    public String showCreateForm(@PathVariable Integer orderItemId,
                                 Model model,
                                 Authentication authentication) {
        setCommonAttributes(model, authentication);

        Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(orderItemId);
        if (orderItemOpt.isEmpty()) {
            model.addAttribute("errorMessage", "OrderItem nuk u gjet.");
            return "redirect:/order-items";
        }

        // Ngarkojmë të gjitha shërbimet që mund të zgjidhen
        Iterable<Services> availableServices = serviceService.getAllServices();

        model.addAttribute("orderItem", orderItemOpt.get());
        model.addAttribute("availableServices", availableServices);
        model.addAttribute("orderServiceCreateDTO", new OrderServiceCreateDTO());
        model.addAttribute("orderItemId", orderItemId);

        return "orderservice-form"; // templates/orderservice-form.html
    }

    // 4. Ruajtja e shërbimit të ri (POST)
    @PostMapping("/add/{orderItemId}")
    public String createOrderService(@PathVariable Integer orderItemId,
                                     @Valid @ModelAttribute("orderServiceCreateDTO") OrderServiceCreateDTO dto,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        setCommonAttributes(model, authentication);

        // Nëse ka gabime validimi
        if (result.hasErrors()) {
            Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(orderItemId);
            model.addAttribute("orderItem", orderItemOpt.orElse(null));
            model.addAttribute("availableServices", serviceService.getAllServices());
            model.addAttribute("orderItemId", orderItemId);
            return "orderservice-form";
        }

        // Vendosim manualisht orderItemId në DTO (sepse vjen nga URL)
        dto.setOrderItemId(orderItemId);

        ResponseEntity<Map<String, Object>> response = orderServiceService.createOrderService(dto);

        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Shërbimi u shtua me sukses!");
            return "redirect:/order-services/by-order-item/" + orderItemId;
        } else {
            Map<String, Object> body = response.getBody();
            String errorMsg = body != null ? (String) body.get("error") : "Gabim i panjohur";
            model.addAttribute("errorMessage", errorMsg);

            model.addAttribute("orderItem", orderItemService.getOrderItemById(orderItemId).orElse(null));
            model.addAttribute("availableServices", serviceService.getAllServices());
            model.addAttribute("orderItemId", orderItemId);
            return "orderservice-form";
        }
    }

    // 5. Forma për editim (GET)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,
                               Model model,
                               Authentication authentication) {
        setCommonAttributes(model, authentication);

        Optional<OrderService> osOpt = orderServiceService.getOrderServiceById(id);
        if (osOpt.isEmpty()) {
            model.addAttribute("errorMessage", "OrderService nuk u gjet.");
            return "redirect:/order-services";
        }

        OrderService os = osOpt.get();
        OrderServiceUpdateDTO updateDTO = new OrderServiceUpdateDTO();
        updateDTO.setQuantity(os.getQuantity());
        updateDTO.setPrice(os.getPrice());

        model.addAttribute("orderServiceUpdateDTO", updateDTO);
        model.addAttribute("orderService", os);
        model.addAttribute("orderItemId", os.getOrderItem().getId());

        return "orderservice-edit"; // templates/orderservice-edit.html
    }

    // 6. Update (POST)
    @PostMapping("/edit/{id}")
    public String updateOrderService(@PathVariable Integer id,
                                     @Valid @ModelAttribute("orderServiceUpdateDTO") OrderServiceUpdateDTO dto,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        setCommonAttributes(model, authentication);

        if (result.hasErrors()) {
            Optional<OrderService> osOpt = orderServiceService.getOrderServiceById(id);
            model.addAttribute("orderService", osOpt.orElse(null));
            if (osOpt.isPresent()) {
                model.addAttribute("orderItemId", osOpt.get().getOrderItem().getId());
            }
            return "orderservice-edit";
        }

        ResponseEntity<Map<String, Object>> response = orderServiceService.updateOrderService(id, dto);

        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Shërbimi u përditësua me sukses!");
            OrderService updated = (OrderService) response.getBody().get("data");
            return "redirect:/order-services/by-order-item/" + updated.getOrderItem().getId();
        } else {
            Map<String, Object> body = response.getBody();
            model.addAttribute("errorMessage", body != null ? body.get("error") : "Gabim gjatë ruajtjes");
            model.addAttribute("orderService", orderServiceService.getOrderServiceById(id).orElse(null));
            return "orderservice-edit";
        }
    }

    // 7. Fshirja
    @GetMapping("/delete/{id}")
    public String deleteOrderService(@PathVariable Integer id,
                                     RedirectAttributes redirectAttributes) {
        try {
            Optional<OrderService> osOpt = orderServiceService.getOrderServiceById(id);
            if (osOpt.isPresent()) {
                Integer orderItemId = osOpt.get().getOrderItem().getId();
                orderServiceService.deleteOrderServiceById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Shërbimi u fshi me sukses!");
                return "redirect:/order-services/by-order-item/" + orderItemId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order-services";
    }

    // === Metodë ndihmëse për atributet e përbashkëta ===
    private void setCommonAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("");
            model.addAttribute("firstName", username);
            model.addAttribute("role", role);
        }
    }
}