package com.sportshop.api.Domain.Reponse.VNPay;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO response cho VNPay payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayPaymentResponse {
    private String paymentUrl;
    private String orderId;
    private Long amount;
    private String orderInfo;
    private LocalDateTime createDate;
    private String transactionId;
}