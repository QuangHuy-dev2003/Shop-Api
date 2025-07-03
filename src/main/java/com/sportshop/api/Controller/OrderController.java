package com.sportshop.api.Controller;

import com.sportshop.api.Domain.Request.Order.PlaceOrderRequest;
import com.sportshop.api.Domain.Reponse.Order.OrderResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
}
