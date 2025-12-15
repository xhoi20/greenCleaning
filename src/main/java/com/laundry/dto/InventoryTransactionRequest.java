package com.laundry.dto;



import com.laundry.entity.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryTransactionRequest {
    private int supplyId;
    private int quantity;
    private TransactionType transactionType;
    private String notes;
    private BigDecimal unitCost;
}
