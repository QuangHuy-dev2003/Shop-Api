package com.sportshop.api.Domain.Reponse.Order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

/**
 * DTO trả về thống kê đơn hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {
    private Long totalOrders;
    private Long totalRevenue;
    private Map<String, Long> orderCountByStatus; // trạng thái -> số lượng
    private Map<String, Long> revenueByStatus; // trạng thái -> doanh thu
}