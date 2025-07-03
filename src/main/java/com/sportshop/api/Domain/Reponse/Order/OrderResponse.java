package com.sportshop.api.Domain.Reponse.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về thông tin đơn hàng sau khi đặt thành công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String status;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private Long totalAmount;
    private Long discountAmount;
    private List<OrderItemInfo> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long productId;
        private String productName;
        private String size;
        private String color;
        private Integer quantity;
        private Long unitPrice;
        private Long subtotal;
        private String imageUrl;
    }
}