package com.laundry.dto;


import com.laundry.entity.OrderStatus;
import com.laundry.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    @NotNull(message = "Customer ID nuk mund të jetë null")
    private Integer customerId;  // ID i klientit
    private int id;
    @NotNull(message = "Dropoff date nuk mund të jetë null")
    @FutureOrPresent(message = "Dropoff date duhet të jetë sot ose në të ardhmen")
    private LocalDate dropoffDate;  // Data e dorëzimit

    private LocalDate pickupDate;  // Data e marrjes (opsionale)
    private OrderStatus status = OrderStatus.RECEIVED;
    private String notes;
    private List<OrderItemCreateDTO> orderItems;

    private List<OrderServiceCreateDTO> orderServices = new ArrayList<>();

    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
}