package com.laundry.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Inventory_Transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
    // ✅ NEW: kosto për 1 njësi (p.sh. 2.50)
    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    // ✅ NEW: total = quantity * unitCost (opsionale, por praktike)
    @Column(name = "total_cost", precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "notes", columnDefinition = "VARCHAR(MAX)")
    private String notes;

    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "id=" + id +
                ", supplyId=" + (supply != null ? supply.getId() : null) +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", quantity=" + quantity +
                ", transactionType=" + transactionType +
                ", transactionDate=" + transactionDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}