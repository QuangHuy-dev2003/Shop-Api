package com.sportshop.api.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

import com.sportshop.api.Service.DiscountsService;
import com.sportshop.api.Domain.Request.Discounts.CreateDiscountRequest;
import com.sportshop.api.Domain.Reponse.Discounts.DiscountResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class DiscountsController {

    private final DiscountsService discountsService;

    public DiscountsController(DiscountsService discountsService) {
        this.discountsService = discountsService;
    }

    /**
     * Lấy tất cả mã giảm giá
     */
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getAllDiscounts() {
        List<DiscountResponse> discounts = discountsService.getAllDiscounts();
        return ResponseEntity.ok(ApiResponse.success(discounts, "Lấy danh sách mã giảm giá thành công"));
    }

    /**
     * Lấy mã giảm giá theo ID
     */
    @GetMapping("/discounts/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(@PathVariable Long id) {
        DiscountResponse discount = discountsService.getDiscountById(id);
        return ResponseEntity.ok(ApiResponse.success(discount, "Lấy mã giảm giá thành công"));
    }

    /**
     * Lấy mã giảm giá theo code
     */
    @GetMapping("/discounts/code/{code}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountByCode(@PathVariable String code) {
        DiscountResponse discount = discountsService.getDiscountByCode(code);
        return ResponseEntity.ok(ApiResponse.success(discount, "Lấy mã giảm giá thành công"));
    }

    /**
     * Tạo mới mã giảm giá
     */
    @PostMapping("/discounts")
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(
            @Valid @RequestBody CreateDiscountRequest request) {
        DiscountResponse created = discountsService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Tạo mã giảm giá thành công"));
    }

    /**
     * Cập nhật mã giảm giá
     */
    @PutMapping("/discounts/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody CreateDiscountRequest request) {
        DiscountResponse updated = discountsService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật mã giảm giá thành công"));
    }

    /**
     * Xóa mã giảm giá (soft delete)
     */
    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDiscount(@PathVariable Long id) {
        discountsService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công"));
    }

    /**
     * Kích hoạt/hủy kích hoạt mã giảm giá
     */
    @PatchMapping("/discounts/{id}/toggle")
    public ResponseEntity<ApiResponse<DiscountResponse>> toggleDiscountStatus(@PathVariable Long id) {
        DiscountResponse updated = discountsService.toggleDiscountStatus(id);
        String message = updated.getIsActive() ? "Kích hoạt mã giảm giá thành công"
                : "Hủy kích hoạt mã giảm giá thành công";
        return ResponseEntity.ok(ApiResponse.success(updated, message));
    }

    /**
     * Validate mã giảm giá (dùng khi user nhập mã)
     */
    @PostMapping("/discounts/validate")
    public ResponseEntity<ApiResponse<DiscountResponse>> validateDiscountCode(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        DiscountResponse validation = discountsService.validateDiscountCode(code, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(validation, validation.getStatusMessage()));
    }
}
