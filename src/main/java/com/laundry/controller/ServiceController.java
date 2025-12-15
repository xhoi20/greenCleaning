package com.laundry.controller;

import com.laundry.dto.ServiceCreateDTO;
import com.laundry.dto.ServiceUpdateDTO;
import com.laundry.entity.Services;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // Listë e të gjithë shërbimeve
    @GetMapping
    @Transactional(readOnly = true)
    public String getAllServices(Model model, Authentication authentication) {
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
        Iterable<Services> services = serviceService.getAllServices();
        model.addAttribute("services", services);
        return "service-list";  // Template: service-list.html
    }

    // Formë për Shto të Ri (GET) - VEÇANëRISHT PëR CREATE
    @GetMapping("/add")
    public String showCreateServiceForm(Model model, Authentication authentication) {
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
        model.addAttribute("serviceCreateDTO", new ServiceCreateDTO());
        return "service-form";  // VEÇANëRISHT PëR CREATE
    }

    // Shto të Ri (POST) - VEÇANëRISHT PëR CREATE
    @PostMapping("/add")
    public String createService(@Valid @ModelAttribute("serviceCreateDTO") ServiceCreateDTO serviceCreateDTO,
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
            model.addAttribute("serviceCreateDTO", serviceCreateDTO);
            return "service-form";  // Rikthe te form create nëse ka gabime
        }

        ResponseEntity<Map<String, Object>> response = serviceService.createService(serviceCreateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Service created successfully!");
            return "redirect:/services";
        } else {
            model.addAttribute("serviceCreateDTO", serviceCreateDTO);
            model.addAttribute("errorMessage", response.getBody().get("error"));
            return "service-form";  // Rikthe te form create
        }
    }

    // Formë për Ndrysho (GET) - VEÇANëRISHT PëR UPDATE
    @GetMapping("/edit/{id}")
    public String showEditServiceForm(@PathVariable Integer id, Model model, Authentication authentication) {
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

        Optional<Services> serviceOpt = serviceService.getServiceById(id);
        if (serviceOpt.isPresent()) {
            Services service = serviceOpt.get();
            ServiceUpdateDTO updateDTO = ServiceUpdateDTO.builder()
                    .id(service.getId())
                    .name(service.getName())
                    .description(service.getDescription())
                    .pricePerUnit(service.getPricePerUnit())
                    .unitType(service.getUnitType()) // != null ? service.getUnitType().name() : null)  // FIX: Plotësuar kondicionalin
                    .estimatedTime(service.getEstimatedTime())
                    .build();
            model.addAttribute("serviceUpdateDTO", updateDTO);
            model.addAttribute("serviceId", id);
            return "edit-service";  // VEÇANëRISHT PëR UPDATE
        } else {
            model.addAttribute("errorMessage", "Service nuk u gjet me ID: " + id);
            return "redirect:/services";
        }
    }

    // Ndrysho (POST) - VEÇANëRISHT PëR UPDATE
    @PostMapping("/edit/{id}")
    public String updateService(@PathVariable Integer id,
                                @Valid @ModelAttribute("serviceUpdateDTO") ServiceUpdateDTO serviceUpdateDTO,
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
            model.addAttribute("serviceUpdateDTO", serviceUpdateDTO);
            model.addAttribute("serviceId", id);
            return "edit-service";  // Rikthe te form edit nëse ka gabime
        }

        ResponseEntity<Map<String, Object>> response = serviceService.updateService(id, serviceUpdateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Service updated successfully!");
            return "redirect:/services";
        } else {
            model.addAttribute("serviceUpdateDTO", serviceUpdateDTO);
            model.addAttribute("serviceId", id);
            model.addAttribute("errorMessage", response.getBody().get("error"));
            return "edit-service";  // Rikthe te form edit
        }
    }

    // Fshij
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            serviceService.deleteServiceById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/services";
    }
}