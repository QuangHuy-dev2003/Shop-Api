package com.sportshop.api.Domain.Request.Cart;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountRequest {
    private String discountCode;
}