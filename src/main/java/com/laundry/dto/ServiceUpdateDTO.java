package com.laundry.dto;

import com.laundry.entity.UnitType;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceUpdateDTO {
   private int id;
    private String name;  // Optional
    private String description;  // Optional
    private BigDecimal pricePerUnit;  // Optional
    private UnitType unitType;  // Optional
    private Integer estimatedTime;  // Optional
}