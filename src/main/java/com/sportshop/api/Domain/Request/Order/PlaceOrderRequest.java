package com.sportshop.api.Domain.Request.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO nhận thông tin đặt hàng từ client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    @NotBlank(message = "Phương thức vận chuyển không được để trống")
    private String shippingMethod;

    private List<String> discountCodes;
}