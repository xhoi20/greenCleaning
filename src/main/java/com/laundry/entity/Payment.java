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
@Table(name = "Payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

@Column(name = "amount", nullable = false, precision = 8, scale = 2, columnDefinition = "DECIMAL(8,2)")
private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'pending'")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", paymentDate=" + paymentDate +
                ", status=" + status +
                '}';
    }
}