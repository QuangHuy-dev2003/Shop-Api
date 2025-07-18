package com.sportshop.api.Domain.Reponse.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO trả về top sản phẩm bán chạy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    private Long productId;
    private String productName;
    private Long totalSold;
    private Long totalRevenue;
    private String productImage;
}