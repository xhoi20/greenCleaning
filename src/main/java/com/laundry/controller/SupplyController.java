package com.laundry.controller;

import com.laundry.entity.Supply;
import com.laundry.entity.SupplyUnit;
import com.laundry.service.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/supplies")
@RequiredArgsConstructor
public class SupplyController {
    private final SupplyService supplyService;

    // LIST
    @GetMapping
    public String listSupplies(Model model) {
        model.addAttribute("supplies", supplyService.getAllSupplies());
        return "supplies/list"; // templates/supplies/list.html
    }

    // FORM KRIJIMI
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("supply", new Supply());
        model.addAttribute("units", Arrays.asList(SupplyUnit.values()));
        return "supplies/form"; // templates/supplies/form.html
    }

    // RUAN KRIJIMIN
    @PostMapping
    public String createSupply(@ModelAttribute("supply") Supply supply,
                               RedirectAttributes redirectAttributes) {
        supplyService.createSupply(supply);
        redirectAttributes.addFlashAttribute("successMessage", "Supply created successfully!");
        return "redirect:/supplies";
    }

    // FORM EDITIMI
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable int id, Model model) {
        Supply supply = supplyService.getSupplyById(id);
        model.addAttribute("supply", supply);
        model.addAttribute("units", Arrays.asList(SupplyUnit.values()));
        return "supplies/form";
    }

    // RUAN EDITIMIN
    @PostMapping("/{id}")
    public String updateSupply(@PathVariable int id,
                               @ModelAttribute("supply") Supply supply,
                               RedirectAttributes redirectAttributes) {
        supplyService.updateSupply(id, supply);
        redirectAttributes.addFlashAttribute("successMessage", "Supply updated successfully!");
        return "redirect:/supplies";
    }

    // FSHIRJE
    @PostMapping("/{id}/delete")
    public String deleteSupply(@PathVariable int id,
                               RedirectAttributes redirectAttributes) {
        supplyService.deleteSupply(id);
        redirectAttributes.addFlashAttribute("successMessage", "Supply deleted successfully!");
        return "redirect:/supplies";
    }
}
