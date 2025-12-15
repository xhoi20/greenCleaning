package com.laundry.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Supplies")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", length = 10)
    private SupplyUnit unit;

    @Column(name = "reorder_level", columnDefinition = "INT DEFAULT 10")
    private int reorderLevel = 10;

    @Column(name = "current_stock", columnDefinition = "INT DEFAULT 0")
    private int currentStock = 0;

    @OneToMany(mappedBy = "supply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<InventoryTransaction> inventoryTransactions;

    @Override
    public String toString() {
        return "Supply{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", unit=" + unit +
                ", reorderLevel=" + reorderLevel +
                ", currentStock=" + currentStock +
                '}';
    }
}