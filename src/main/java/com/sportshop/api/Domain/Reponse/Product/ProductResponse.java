package com.sportshop.api.Domain.Reponse.Product;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer sale;
    private BigDecimal salePrice;
    private Boolean isActive;
    private String imageUrl;
    private LocalDateTime createdAt;
    private CategoryResponse category;
    private BrandResponse brand;
    private List<String> additionalImages;
    private List<ProductVariantResponse> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandResponse {
        private Long id;
        private String name;
        private String description;
        private String logoUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantResponse {
        private Long id;
        private String size;
        private Integer stockQuantity;
        private BigDecimal price;
    }
}