package com.laundry.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemUpdateDTO {
    private int id;
    private String itemDescription;

    @Min(value = 1, message = "Sasia duhet të jetë së paku 1")
    private int quantity;

    @DecimalMin(value = "0.00", message = "Çmimi unitar nuk mund të jetë negativ")
    private BigDecimal unitPrice;

    // Opsionale: tagId mund të përditësohet nëse nevojitet
    private String tagId;
}