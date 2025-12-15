package com.laundry.service;



import com.laundry.dto.InventoryTransactionRequest;
import com.laundry.entity.InventoryTransaction;
import com.laundry.entity.Supply;
import com.laundry.entity.TransactionType;
import com.laundry.repository.InventoryTransactionRepository;
import com.laundry.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final SupplyRepository supplyRepository;

    public List<InventoryTransaction> getAllTransactions() {
        return inventoryTransactionRepository.findAll();
    }

    public List<InventoryTransaction> getTransactionsBySupplyId(int supplyId) {
        return inventoryTransactionRepository.findBySupplyId(supplyId);
    }

    public InventoryTransaction getById(int id) {
        return inventoryTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InventoryTransaction not found with id: " + id));
    }

//    @Transactional
//    public InventoryTransaction createTransaction(InventoryTransactionRequest request) {
//        Supply supply = supplyRepository.findById(request.getSupplyId())
//                .orElseThrow(() -> new RuntimeException("Supply not found with id: " + request.getSupplyId()));
//
//        // Krijojmë transaksionin e ri
//        InventoryTransaction transaction = InventoryTransaction.builder()
//                .supply(supply)
//                .quantity(request.getQuantity())
//                .transactionType(request.getTransactionType())
//                .notes(request.getNotes())
//                .transactionDate(LocalDateTime.now())
//                .build();
//
//        // Përditësimi i stock-ut në varesi të TransactionType
//        int currentStock = supply.getCurrentStock();
//        int quantity = request.getQuantity();
//
//        if (request.getTransactionType() == TransactionType.PURCHASE) {
//            currentStock += quantity;
//        } else if (request.getTransactionType() == TransactionType.USAGE) {
//            currentStock -= quantity;
//            if (currentStock < 0) {
//                throw new RuntimeException("Not enough stock for this usage transaction.");
//            }
//        } else if (request.getTransactionType() == TransactionType.ADJUSTMENT) {
//            // Mund ta interpretosh si:
//            // currentStock = quantity;  // ose +/− quantity, sipas biznes-rregullave të tua
//            currentStock = quantity;
//        }
//
//        supply.setCurrentStock(currentStock);
//        supplyRepository.save(supply);
//
//        return inventoryTransactionRepository.save(transaction);
//    }
@Transactional
public InventoryTransaction createTransaction(InventoryTransactionRequest request) {
    Supply supply = supplyRepository.findById(request.getSupplyId())
            .orElseThrow(() -> new RuntimeException("Supply not found with id: " + request.getSupplyId()));

    BigDecimal unitCost = request.getUnitCost();
    BigDecimal totalCost = null;

    // vetëm për PURCHASE e detyrojmë çmimin
    if (request.getTransactionType() == TransactionType.PURCHASE) {
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Unit cost is required for PURCHASE and must be > 0.");
        }
        totalCost = unitCost.multiply(BigDecimal.valueOf(request.getQuantity()));
    }

    InventoryTransaction transaction = InventoryTransaction.builder()
            .supply(supply)
            .quantity(request.getQuantity())
            .transactionType(request.getTransactionType())
            .notes(request.getNotes())
            .transactionDate(LocalDateTime.now())
            .unitCost(unitCost)
            .totalCost(totalCost)
            .build();

    int currentStock = supply.getCurrentStock();
    int quantity = request.getQuantity();

    if (request.getTransactionType() == TransactionType.PURCHASE) {
        currentStock += quantity;
    } else if (request.getTransactionType() == TransactionType.USAGE) {
        currentStock -= quantity;
        if (currentStock < 0) throw new RuntimeException("Not enough stock for this usage transaction.");
    } else if (request.getTransactionType() == TransactionType.ADJUSTMENT) {
        currentStock = quantity;
    }

    supply.setCurrentStock(currentStock);
    supplyRepository.save(supply);

    return inventoryTransactionRepository.save(transaction);
}
    public void deleteTransaction(int id) {
        // Opsionale: mund të kthesh mbrapa efektin në stock, por këtu po e lë të thjeshtë
        InventoryTransaction tx = getById(id);
        inventoryTransactionRepository.delete(tx);
    }
}
