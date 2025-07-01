package com.sportshop.api.Controller;

import com.sportshop.api.Domain.Request.Cart.AddOrUpdateCartItemRequest;
import com.sportshop.api.Domain.Reponse.Cart.CartItemResponse;
import com.sportshop.api.Domain.Reponse.Cart.CartResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Lấy thông tin giỏ hàng của người dùng theo userId
     * 
     * @param userId ID người dùng
     * @return Thông tin giỏ hàng
     */
    @GetMapping("/cart/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCartResponseByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Lấy thông tin giỏ hàng thành công"));
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của người dùng
     * 
     * @param userId ID người dùng
     * @return Danh sách sản phẩm trong giỏ hàng
     */
    @GetMapping("/cart/{userId}/items")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItems(@PathVariable Long userId) {
        List<CartItemResponse> items = cartService.getCartItemResponses(userId);
        return ResponseEntity.ok(ApiResponse.success(items, "Lấy danh sách sản phẩm trong giỏ hàng thành công"));
    }

    /**
     * Thêm mới sản phẩm vào giỏ hàng
     * 
     * @param userId  ID người dùng
     * @param request Thông tin sản phẩm cần thêm
     * @return Sản phẩm đã thêm vào giỏ hàng
     */
    @PostMapping("/cart/{userId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddOrUpdateCartItemRequest request) {
        cartService.addCartItem(userId, request);
        CartResponse cart = cartService.getCartResponseByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Thêm sản phẩm vào giỏ hàng thành công"));
    }

    /**
     * Cập nhật sản phẩm trong giỏ hàng
     * 
     * @param userId  ID người dùng
     * @param request Thông tin sản phẩm cần cập nhật
     * @return Sản phẩm đã cập nhật trong giỏ hàng
     */
    @PutMapping("/cart/{userId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddOrUpdateCartItemRequest request) {
        cartService.updateCartItem(userId, request);
        CartResponse cart = cartService.getCartResponseByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cập nhật sản phẩm trong giỏ hàng thành công"));
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     * 
     * @param userId    ID người dùng
     * @param productId ID sản phẩm
     * @param variantId ID biến thể (nếu có)
     * @return Thông tin giỏ hàng sau khi xóa
     */
    @DeleteMapping("/cart/{userId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId) {
        cartService.removeCartItem(userId, productId, variantId);
        CartResponse cart = cartService.getCartResponseByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa sản phẩm khỏi giỏ hàng thành công"));
    }

    /**
     * Xóa toàn bộ sản phẩm trong giỏ hàng
     * 
     * @param userId ID người dùng
     * @return Thông tin giỏ hàng sau khi xóa
     */
    @DeleteMapping("/cart/{userId}/items/all")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        CartResponse cart = cartService.getCartResponseByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa toàn bộ sản phẩm trong giỏ hàng thành công"));
    }

    /**
     * Xóa giỏ hàng của người dùng
     * 
     * @param userId ID người dùng
     * @return Trạng thái xóa
     */
    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteCart(@PathVariable Long userId) {
        cartService.deleteCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa giỏ hàng thành công"));
    }

}