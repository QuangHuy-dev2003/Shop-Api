package com.sportshop.api.Controller;

import com.sportshop.api.Domain.Request.Order.PlaceOrderRequest;
import com.sportshop.api.Domain.Reponse.Order.OrderResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import com.sportshop.api.Domain.Request.Order.OrderUpdateRequest;
import java.util.List;
import com.sportshop.api.Domain.Reponse.Order.OrderStatisticsResponse;
import com.sportshop.api.Domain.Reponse.Order.MonthlyRevenueResponse;
import com.sportshop.api.Domain.Reponse.Order.TopProductResponse;
import com.sportshop.api.Domain.Reponse.Order.DashboardStatisticsResponse;

/**
 * Controller xử lý các API liên quan đến đặt hàng (Order)
 */
@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API đặt hàng cho người dùng
     * 
     * @param request Thông tin đặt hàng (userId, shippingAddressId, paymentMethod,
     *                discountCode)
     * @return Thông tin đơn hàng vừa tạo
     */
    @PostMapping("/orders/place-order")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đặt hàng thành công!"));
    }

    /**
     * Lấy tất cả đơn hàng (admin, có phân trang, lọc)
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "orderCode", required = false) String orderCode,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo) {
        Page<OrderResponse> result = orderService.getAllOrders(page, size, status, userId, orderCode, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đơn hàng thành công!"));
    }

    /**
     * Lấy đơn hàng theo ID
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable("orderId") Long orderId) {
        OrderResponse result = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy đơn hàng thành công!"));
    }

    /**
     * Lấy đơn hàng theo userId
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByUserId(@PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<OrderResponse> result = orderService.getOrdersByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy đơn hàng theo user thành công!"));
    }

    /**
     * Tìm kiếm đơn hàng theo mã
     */
    @GetMapping("/orders/search")
    public ResponseEntity<ApiResponse<OrderResponse>> searchOrderByCode(@RequestParam("orderCode") String orderCode) {
        OrderResponse result = orderService.searchOrderByCode(orderCode);
        return ResponseEntity.ok(ApiResponse.success(result, "Tìm kiếm đơn hàng thành công!"));
    }

    /**
     * Thống kê đơn hàng
     */
    @GetMapping("/orders/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        OrderStatisticsResponse result = orderService.getOrderStatistics(from, to);
        return ResponseEntity.ok(ApiResponse.success(result, "Thống kê đơn hàng thành công!"));
    }

    /**
     * Thống kê doanh thu theo tháng/năm
     */
    @GetMapping("/orders/revenue-by-month")
    public ResponseEntity<ApiResponse<List<MonthlyRevenueResponse>>> getMonthlyRevenue(
            @RequestParam(value = "year", defaultValue = "2025") int year) {
        List<MonthlyRevenueResponse> result = orderService.getMonthlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success(result, "Thống kê doanh thu theo tháng thành công!"));
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    @GetMapping("/orders/top-products")
    public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<TopProductResponse> result = orderService.getTopProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy top sản phẩm bán chạy thành công!"));
    }

    /**
     * Thống kê tổng quan cho dashboard
     */
    @GetMapping("/orders/dashboard-statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getDashboardStatistics() {
        DashboardStatisticsResponse result = orderService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(result, "Thống kê dashboard thành công!"));
    }

    /**
     * Cập nhật đơn hàng (trạng thái, thông tin nhận hàng)
     */
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(@PathVariable("orderId") Long orderId,
            @Valid @RequestBody OrderUpdateRequest updateRequest) {
        OrderResponse result = orderService.updateOrder(orderId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(result, "Cập nhật đơn hàng thành công!"));
    }

    /**
     * Xoá đơn hàng
     */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<String>> deleteOrder(@PathVariable("orderId") Long orderId) {
        boolean ok = orderService.deleteOrder(orderId);
        if (ok)
            return ResponseEntity.ok(ApiResponse.success("Xoá đơn hàng thành công!"));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error("Xoá đơn hàng thất bại!"));
    }

    /**
     * Hủy đơn hàng (user)
     */
    @PostMapping("/orders/{orderId}/cancel/user")
    public ResponseEntity<ApiResponse<String>> cancelOrderByUser(@PathVariable("orderId") Long orderId,
            @RequestParam("userId") Long userId) {
        boolean ok = orderService.cancelOrderByUser(orderId, userId);
        if (ok)
            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng (user) thành công!"));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error("Hủy đơn hàng (user) thất bại!"));
    }

    /**
     * Hủy đơn hàng (admin)
     */
    @PostMapping("/orders/{orderId}/cancel/admin")
    public ResponseEntity<ApiResponse<String>> cancelOrderByAdmin(@PathVariable("orderId") Long orderId,
            @RequestParam("adminId") Long adminId) {
        boolean ok = orderService.cancelOrderByAdmin(orderId, adminId);
        if (ok)
            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng (admin) thành công!"));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error("Hủy đơn hàng (admin) thất bại!"));
    }
}
