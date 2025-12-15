package com.laundry.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Services")
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    private String description;

@Column(name = "price_per_unit", nullable = false, precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2)")
private BigDecimal pricePerUnit;
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'item'")
    private UnitType unitType = UnitType.ITEM;

    @Column(name = "estimated_time")
    private Integer estimatedTime;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderService> orderServices;

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", pricePerUnit=" + pricePerUnit +
                ", unitType=" + unitType +
                ", estimatedTime=" + estimatedTime +
                '}';
    }
}