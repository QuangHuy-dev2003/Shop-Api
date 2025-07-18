package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.sportshop.api.Service.ShippingAddressService;
import com.sportshop.api.Domain.Request.ShippingAddress.CreateAddressRequest;
import com.sportshop.api.Domain.Request.ShippingAddress.UpdateAddressRequest;
import com.sportshop.api.Domain.Reponse.User.ShippingAddressResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    public ShippingAddressController(ShippingAddressService shippingAddressService) {
        this.shippingAddressService = shippingAddressService;
    }

    /**
     * Lấy danh sách địa chỉ của user
     */
    @GetMapping("/shipping-addresses/{userId}")
    public ResponseEntity<ApiResponse<List<ShippingAddressResponse>>> getUserAddresses(
            @PathVariable("userId") Long userId) {
        List<ShippingAddressResponse> addresses = shippingAddressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Lấy danh sách địa chỉ thành công"));
    }

    /**
     * Tạo địa chỉ mới
     */
    @PostMapping("shipping-addresses/{userId}")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> createAddress(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody CreateAddressRequest request) {
        ShippingAddressResponse address = shippingAddressService.createAddress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Tạo địa chỉ thành công"));
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("shipping-addresses/{userId}/{addressId}")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> updateAddress(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        ShippingAddressResponse address = shippingAddressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Cập nhật địa chỉ thành công"));
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @PutMapping("/shipping-addresses/{userId}/{addressId}/set-default")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> setDefaultAddress(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId) {
        ShippingAddressResponse address = shippingAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Đặt địa chỉ mặc định thành công"));
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("shipping-addresses/{userId}/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId) {
        shippingAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công"));
    }
}