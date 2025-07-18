package com.sportshop.api.Domain.Reponse.Cart;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalQuantity;
    private BigDecimal totalPrice;
    private BigDecimal subtotal; // Giá trước khi áp mã giảm giá
    private String discountCode;
    private java.math.BigDecimal discountAmount;
    private List<String> discountCodes;
    private Boolean freeShipping = false;
    private BigDecimal freeShippingAmount;
}