package com.laundry.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderServiceUpdateDTO {
    private Integer quantity;  // Optional, >0
    private BigDecimal price;  // Optional, >0
}