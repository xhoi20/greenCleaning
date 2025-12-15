package com.laundry.dto;


import com.laundry.entity.AssignedRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEmployeeCreateDTO {
    private Integer orderId;  // ID e order-it (nuk e setojmë direkt entity-n)
    private Integer employeeId;  // ID e punonjësit
    private AssignedRole assignedRole;  // Roli i asignuar
    // assignedAt setohet automatikisht në service
}