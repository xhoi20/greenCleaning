package com.laundry.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Order_Employees")
public class OrderEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_role", length = 20)
    private AssignedRole assignedRole;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "OrderEmployee{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", employeeId=" + (employee != null ? employee.getId() : null) +
                ", assignedRole=" + assignedRole +
                ", assignedAt=" + assignedAt +
                '}';
    }
}