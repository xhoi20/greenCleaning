package com.laundry.dto;
import com.laundry.entity.PaymentMethod;
import com.laundry.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentUpdateDTO {
    private Integer id;
    private BigDecimal amount;  // Optional, >0
    private PaymentMethod paymentMethod;  // Optional
    private PaymentStatus status;
}
