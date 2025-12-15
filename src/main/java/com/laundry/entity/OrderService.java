package com.laundry.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(name = "Order_Services")
public class OrderService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;
    @Column(name = "service_name", length = 255)
    private String serviceName;
    @Column(name = "quantity", columnDefinition = "INT DEFAULT 1")
    private int quantity = 1;


@Column(name = "price", precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2)")
private BigDecimal price;

    @Override
    public String toString() {
        return "OrderService{" +
                "id=" + id +
                ", orderItemId=" + (orderItem != null ? orderItem.getId() : null) +
                ", serviceId=" + (service != null ? service.getId() : null) +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}