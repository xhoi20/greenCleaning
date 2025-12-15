package com.laundry.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "dropoff_date", nullable = false)
    private LocalDate dropoffDate;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'received'")
    private OrderStatus status = OrderStatus.RECEIVED;

@Column(name = "total_amount", precision = 8, scale = 2, columnDefinition = "DECIMAL(8,2) DEFAULT 0.00")
private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "VARCHAR(MAX)")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<OrderEmployee> orderEmployees;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Payment> payments;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<InventoryTransaction> inventoryTransactions;

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", dropoffDate=" + dropoffDate +
                ", pickupDate=" + pickupDate +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", notes='" + notes + '\'' +
                '}';
    }
}