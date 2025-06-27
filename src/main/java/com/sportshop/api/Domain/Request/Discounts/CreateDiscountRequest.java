package com.sportshop.api.Domain.Request.Discounts;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sportshop.api.Domain.Discounts.DiscountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiscountRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 100, message = "Mã giảm giá không được vượt quá 100 ký tự")
    private String code;

    @NotBlank(message = "Tên mã giảm giá không được để trống")
    @Size(max = 255, message = "Tên mã giảm giá không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    private BigDecimal discountValue;

    @Positive(message = "Giá trị đơn hàng tối thiểu phải là số dương")
    private BigDecimal minimumOrderAmount;

    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    private Integer usageLimit;

    @Min(value = 1, message = "Giới hạn sử dụng mỗi user phải lớn hơn 0")
    private Integer perUserLimit;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive = true;
}