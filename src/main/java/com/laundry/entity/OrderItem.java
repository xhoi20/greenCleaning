package com.laundry.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Order_Items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "item_description", nullable = false, length = 100)
    private String itemDescription;

    @Column(name = "quantity", columnDefinition = "INT DEFAULT 1")
    private int quantity = 1;

@Column(name = "unit_price", precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2)")
private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 6, scale = 2, columnDefinition = "DECIMAL(6,2)")
    private BigDecimal subtotal;
    @Column(name = "tag_id", length = 20, unique = true)
    private String tagId;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<OrderService> orderServices;

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", itemDescription='" + itemDescription + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                ", tagId='" + tagId + '\'' +
                '}';
    }
}