package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.sportshop.api.Service.FavoritesService;
import com.sportshop.api.Domain.Request.Favorites.ToggleFavoriteRequest;
import com.sportshop.api.Domain.Reponse.Favorites.FavoriteResponse;
import com.sportshop.api.Domain.Reponse.Favorites.ToggleFavoriteResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FavoritesController {

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    /**
     * Toggle favorite: thêm nếu chưa có, xóa nếu đã có
     * POST /api/v1/favorites/toggle
     */
    @PostMapping("/favorites/toggle")
    public ResponseEntity<ApiResponse<ToggleFavoriteResponse>> toggleFavorite(
            @Valid @RequestBody ToggleFavoriteRequest request) {
        try {
            ToggleFavoriteResponse response = favoritesService.toggleFavorite(request);
            return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách favorites của user
     * GET /api/v1/favorites/user/{userId}
     */
    @GetMapping("/favorites/user/{userId}")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getUserFavorites(
            @PathVariable("userId") Long userId) {
        try {
            List<FavoriteResponse> favorites = favoritesService.getUserFavorites(userId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Lấy danh sách yêu thích thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Kiểm tra sản phẩm có trong favorites của user không
     * GET /api/v1/favorites/check?userId={userId}&productId={productId}
     */
    @GetMapping("/favorites/check")
    public ResponseEntity<ApiResponse<Boolean>> checkProductInFavorites(
            @RequestParam("userId") Long userId,
            @RequestParam("productId") Long productId) {
        try {
            boolean isFavorite = favoritesService.isProductInFavorites(userId, productId);
            String message = isFavorite ? "Sản phẩm đã có trong danh sách yêu thích"
                    : "Sản phẩm chưa có trong danh sách yêu thích";
            return ResponseEntity.ok(ApiResponse.success(isFavorite, message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi favorites
     * DELETE /api/v1/favorites/user/{userId}/product/{productId}
     */
    @DeleteMapping("/favorites/user/{userId}/product/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromFavorites(
            @PathVariable("userId") Long userId,
            @PathVariable("productId") Long productId) {
        try {
            favoritesService.removeFromFavorites(userId, productId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi danh sách yêu thích"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}