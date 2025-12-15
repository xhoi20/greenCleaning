package com.laundry.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private int id;
    private LocalDate pickupDate;  // Mund të ndryshohet

    @DecimalMin(value = "0.00", message = "Total amount nuk mund të jetë negativ")
    private BigDecimal totalAmount;  // Përditëso totalin (p.sh., pas shtimit items)

    private String notes;  // Përditëso shënimet

    private String status;
}