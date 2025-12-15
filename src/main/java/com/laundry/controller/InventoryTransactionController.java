package com.laundry.controller;

import com.laundry.dto.InventoryTransactionRequest;
import com.laundry.entity.InventoryTransaction;
import com.laundry.entity.Supply;
import com.laundry.entity.TransactionType;
import com.laundry.service.InventoryTransactionService;
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
@RequestMapping("/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;
    private final SupplyService supplyService;

    // LIST E GJITHË TRANSAKSIONEVE
    @GetMapping
    public String listAllTransactions(Model model) {
        model.addAttribute("transactions", inventoryTransactionService.getAllTransactions());
        return "transactions/list"; // templates/transactions/list.html
    }

    // LIST TRANSAKSIONET E NJË SUPPLY
    @GetMapping("/supply/{supplyId}")
    public String listTransactionsBySupply(@PathVariable int supplyId, Model model) {
        Supply supply = supplyService.getSupplyById(supplyId);
        model.addAttribute("supply", supply);
        model.addAttribute("transactions", inventoryTransactionService.getTransactionsBySupplyId(supplyId));
        return "transactions/list-by-supply"; // templates/transactions/list-by-supply.html
    }

    // FORM KRIJIMI PËR NJË SUPPLY SPECIFIK
    @GetMapping("/supply/{supplyId}/new")
    public String showCreateFormForSupply(@PathVariable int supplyId, Model model) {
        InventoryTransactionRequest request = new InventoryTransactionRequest();
        request.setSupplyId(supplyId);

        model.addAttribute("transaction", request);
        model.addAttribute("transactionTypes", Arrays.asList(TransactionType.values()));

        Supply supply = supplyService.getSupplyById(supplyId);
        model.addAttribute("supply", supply);

        return "transactions/form"; // templates/transactions/form.html
    }

    // KRIJO TRANSAKSION
    @PostMapping
    public String createTransaction(@ModelAttribute("transaction") InventoryTransactionRequest request,
                                    RedirectAttributes redirectAttributes) {
        inventoryTransactionService.createTransaction(request);
        redirectAttributes.addFlashAttribute("successMessage", "Transaction created successfully!");
       // return "redirect:/transactions/supply/" + request.getSupplyId();
        return "redirect:/inventory-transactions/supply/" + request.getSupplyId();
    }
}
