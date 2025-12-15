package com.laundry.dto;


import com.laundry.entity.UnitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCreateDTO {
    private Integer orderId;
    private String name;  // Required
    private String description;  // Optional
    private BigDecimal pricePerUnit;  // Required
    private UnitType unitType;  // Optional (default ITEM)
    private Integer estimatedTime;  // Optional
}