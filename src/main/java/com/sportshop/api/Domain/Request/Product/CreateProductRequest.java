package com.sportshop.api.Domain.Request.Product;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import com.sportshop.api.Domain.Product_variants;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải là số dương")
    private BigDecimal price;

    @Positive(message = "Phần trăm giảm giá phải là số dương")
    private Integer sale;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID thương hiệu không được để trống")
    private Long brandId;

    @Size(max = 255, message = "URL hình ảnh không được vượt quá 255 ký tự")
    private String imageUrl;

    private List<String> additionalImages;

    private List<String> imageColors;

    private List<ProductVariantRequest> variants;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã sản phẩm không được vượt quá 50 ký tự")
    private String productCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantRequest {
        @NotNull(message = "Kích thước không được để trống")
        private Product_variants.Size size;

        @NotNull(message = "Màu sắc không được để trống")
        private String color;

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Positive(message = "Số lượng tồn kho phải là số dương")
        private Integer stockQuantity;

        @Positive(message = "Giá biến thể phải là số dương")
        private BigDecimal price;
    }
}