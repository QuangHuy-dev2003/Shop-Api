package com.sportshop.api.Domain.Reponse.Discounts;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sportshop.api.Domain.Discounts.DiscountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer perUserLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thêm trường tính toán
    private Boolean isValid; // Mã có hợp lệ không (còn hạn, còn lượt dùng)
    private String statusMessage; // Thông báo trạng thái
}