package com.laundry.dto;


import com.laundry.entity.EmployeeRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {


        private Integer id;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Role is required")
        private String role;

        private String phone;

        private BigDecimal salary;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate hireDate;

        private boolean active;
    }