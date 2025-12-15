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
public class OrderEmployeeUpdateDTO {
    private AssignedRole assignedRole;  // Vetëm roli mund të përditësohet (opsionale)
    // Id nuk nevojitet, përdoret nga parametri i metodës
}
