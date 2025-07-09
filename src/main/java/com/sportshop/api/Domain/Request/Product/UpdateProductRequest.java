package com.sportshop.api.Domain.Request.Product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import com.sportshop.api.Domain.Product_variants.Size;

@Data
public class UpdateProductRequest {
    private String name;
    private String productCode;
    private String description;
    private BigDecimal price;
    private Integer sale;
    private Boolean isActive;
    private Long categoryId;
    private Long brandId;

    // Ảnh mới sẽ upload, imageColors là màu cho từng ảnh mới
    private List<String> imageColors;

    // Id các ảnh cần xóa
    private List<Long> imageIdsToDelete;

    // Biến thể mới/cập nhật
    private List<VariantDTO> variants;

    // Id các biến thể cần xóa
    private List<Long> variantIdsToDelete;

    @Data
    public static class VariantDTO {
        private Long id; // null nếu là biến thể mới
        private Size size;
        private String color;
        private Integer stockQuantity;
        private BigDecimal price;
    }
}