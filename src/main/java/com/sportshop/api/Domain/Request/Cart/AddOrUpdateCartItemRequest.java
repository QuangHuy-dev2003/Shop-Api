package com.sportshop.api.Domain.Request.Cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOrUpdateCartItemRequest {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    private Long variantId;

    private String size;
    private String color;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}