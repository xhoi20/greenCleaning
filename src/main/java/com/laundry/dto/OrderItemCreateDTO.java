package com.laundry.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCreateDTO {
    private int id;
    @NotNull(message = "Order ID nuk mund të jetë null")
    private Integer orderId;

    @NotBlank(message = "Përshkrimi i item-it nuk mund të jetë bosh")
    private String itemDescription;

    @Min(value = 1, message = "Sasia duhet të jetë së paku 1")
    private int quantity = 1;

    @NotNull(message = "Çmimi unitar nuk mund të jetë null")
    @DecimalMin(value = "0.00", message = "Çmimi unitar nuk mund të jetë negativ")
    private BigDecimal unitPrice;

    // Opsionale: tagId mund të setohet manualisht, por zakonisht gjenerohet
    private String tagId;
}