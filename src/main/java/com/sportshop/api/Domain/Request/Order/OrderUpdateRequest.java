package com.sportshop.api.Domain.Request.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import com.sportshop.api.Domain.Orders;

/**
 * DTO cập nhật đơn hàng: chỉ cho phép cập nhật trạng thái và thông tin nhận
 * hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Orders.OrderStatus status;

    // Thông tin nhận hàng (có thể null nếu không cập nhật)
    private String addressLine;
    private String ward;
    private String district;
    private String province;
}