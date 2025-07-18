package com.sportshop.api.Domain.Reponse.Cart;

import lombok.Data;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal salePrice; // Thêm trường giá sale
    private BigDecimal totalPrice;
    private String imageUrl; // URL ảnh của biến thể màu
    private Integer stockQuantity; // Số lượng tồn kho của biến thể
    private Boolean isOnSale; // Trạng thái sale của sản phẩm
}