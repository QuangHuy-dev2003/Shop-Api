package com.sportshop.api.Domain.Reponse.VNPay;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO response cho VNPay callback
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayCallbackResponse {
    private String responseCode;
    private String responseMessage;
    private String orderId;
    private String transactionId;
    private Long amount;
    private String bankCode;
    private String bankTranNo;
    private String cardType;
    private LocalDateTime payDate;
    private String transactionNo;
    private boolean success;

    // Các trường thông tin lỗi
    private String errorCode;
    private String errorMessage;

    public static VNPayCallbackResponse success(String responseCode, String orderId, String transactionId,
            Long amount, String bankCode, String bankTranNo,
            String cardType, LocalDateTime payDate, String transactionNo) {
        return new VNPayCallbackResponse(
                responseCode, "Giao dịch thành công", orderId, transactionId, amount,
                bankCode, bankTranNo, cardType, payDate, transactionNo, true, null, null);
    }

    public static VNPayCallbackResponse error(String responseCode, String errorCode, String errorMessage) {
        return new VNPayCallbackResponse(
                responseCode, "Giao dịch thất bại", null, null, null,
                null, null, null, null, null, false, errorCode, errorMessage);
    }
}