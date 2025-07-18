package com.sportshop.api.Domain.Reponse.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO trả về thống kê doanh thu theo tháng/năm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private int month;
    private int year;
    private Long totalRevenue;
    private Long totalOrders;
    private String monthName; // Tháng 1, Tháng 2, ...
}