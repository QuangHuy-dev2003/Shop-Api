package com.sportshop.api.Domain.Reponse.VNPay;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO response cho trạng thái giao dịch VNPay
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayTransactionStatusResponse {
    private String transactionId;
    private String orderId;
    private String responseCode;
    private String responseMessage;
    private Long amount;
    private String bankCode;
    private String bankTranNo;
    private String cardType;
    private LocalDateTime payDate;
    private String transactionNo;
    private String transactionStatus;
    private LocalDateTime queryDate;

    // Thông tin lỗi nếu có
    private String errorCode;
    private String errorMessage;
}