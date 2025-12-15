package com.laundry.controller;

import com.laundry.dto.PaymentCreateDTO;
import com.laundry.dto.PaymentUpdateDTO;
import com.laundry.entity.Payment;
import com.laundry.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Helper për të vendosur emrin e user-it dhe rolin në model
    private void addUserInfo(Model model, Authentication authentication) {
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
    }

    // Listë e të gjitha pagesave
    @GetMapping
    @Transactional(readOnly = true)
    public String getAllPayments(Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        Iterable<Payment> payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        return "payment-list"; // Template: payment-list.html
    }

    // Formë për Shto Pagesë (GET)
    @GetMapping("/add")
    public String showCreatePaymentForm(Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("paymentCreateDTO", new PaymentCreateDTO());
        return "payment-form"; // Template: payment-form.html
    }

    // Shto Pagesë (POST)
    @PostMapping("/add")
    public String createPayment(@Valid @ModelAttribute("paymentCreateDTO") PaymentCreateDTO paymentCreateDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                Authentication authentication) {
        addUserInfo(model, authentication);

        if (result.hasErrors()) {
            model.addAttribute("paymentCreateDTO", paymentCreateDTO);
            return "payment-form";
        }

        ResponseEntity<Map<String, Object>> response = paymentService.createPayment(paymentCreateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Payment created successfully!");
            return "redirect:/payments";
        } else {
            model.addAttribute("paymentCreateDTO", paymentCreateDTO);
            Object error = response.getBody() != null ? response.getBody().get("error") : "Unknown error";
            model.addAttribute("errorMessage", error);
            return "payment-form";
        }
    }

    // Formë për Ndryshuar Pagesë (GET)
    @GetMapping("/edit/{id}")
    public String showEditPaymentForm(@PathVariable Integer id,
                                      Model model,
                                      Authentication authentication) {
        addUserInfo(model, authentication);

        Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            PaymentUpdateDTO updateDTO = PaymentUpdateDTO.builder()
                    .id(payment.getId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .build();

            model.addAttribute("paymentUpdateDTO", updateDTO);
            model.addAttribute("paymentId", id);
            return "edit-payment"; // Template: edit-payment.html
        } else {
            model.addAttribute("errorMessage", "Payment not found with ID: " + id);
            return "redirect:/payments";
        }
    }

    // Ndrysho Pagesë (POST)
    @PostMapping("/edit/{id}")
    public String updatePayment(@PathVariable Integer id,
                                @Valid @ModelAttribute("paymentUpdateDTO") PaymentUpdateDTO paymentUpdateDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                Authentication authentication) {
        addUserInfo(model, authentication);

        if (result.hasErrors()) {
            model.addAttribute("paymentUpdateDTO", paymentUpdateDTO);
            model.addAttribute("paymentId", id);
            return "edit-payment";
        }

        ResponseEntity<Map<String, Object>> response = paymentService.updatePayment(id, paymentUpdateDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Payment updated successfully!");
            return "redirect:/payments";
        } else {
            model.addAttribute("paymentUpdateDTO", paymentUpdateDTO);
            model.addAttribute("paymentId", id);
            Object error = response.getBody() != null ? response.getBody().get("error") : "Unknown error";
            model.addAttribute("errorMessage", error);
            return "edit-payment";
        }
    }

    // Fshij Pagesë
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePaymentById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/payments";
    }

    // (Opsionale) Listë pagesash sipas Order-it
    @GetMapping("/order/{orderId}")
    @Transactional(readOnly = true)
    public String getPaymentsByOrder(@PathVariable Integer orderId,
                                     Model model,
                                     Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("payments", paymentService.getPaymentsByOrderId(orderId));
        model.addAttribute("orderId", orderId);
        return "payment-list-by-order";
    }
}
