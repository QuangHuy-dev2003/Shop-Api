package com.sportshop.api.Domain.Request.VNPay;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * DTO request cho việc tạo thanh toán VNPay
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVNPayPaymentRequest {
    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 1000, message = "Số tiền tối thiểu là 1,000 VND")
    private Long amount;

    private String orderInfo;
    private String bankCode; // Mã ngân hàng (để trống = cho phép chọn)
    private String locale = "vn"; // Ngôn ngữ (vn/en)
}