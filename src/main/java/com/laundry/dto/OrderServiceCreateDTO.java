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
public class OrderServiceCreateDTO {
    private Integer orderItemId;  // ID e OrderItem (nuk e setojmë direkt entity-n)
   private Integer serviceId;  // ID e Service (korrigjuar nga Services)
    private int quantity = 1;  // Default 1
    private BigDecimal price;  // Required
    private Integer itemIndex;
   private String serviceName;
}