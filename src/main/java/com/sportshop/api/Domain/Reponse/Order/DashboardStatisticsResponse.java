package com.sportshop.api.Domain.Reponse.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO trả về thống kê tổng quan cho dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private Long totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private List<MonthlyRevenueResponse> monthlyRevenue;
    private List<TopProductResponse> topProducts;
    private Long revenueThisMonth;
    private Long ordersThisMonth;
    private Long newCustomersThisMonth;
}