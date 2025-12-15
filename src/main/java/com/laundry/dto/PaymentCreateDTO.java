package com.laundry.dto;


import com.laundry.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateDTO {
    private Integer orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    // paymentDate dhe status setohen automatikisht në service
}