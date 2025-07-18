package com.sportshop.api.Domain.Request.Cart;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountRequest {
    private List<String> discountCodes;
}