package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.api.Domain.Favorites;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Products;
import com.sportshop.api.Domain.Request.Favorites.ToggleFavoriteRequest;
import com.sportshop.api.Domain.Reponse.Favorites.FavoriteResponse;
import com.sportshop.api.Domain.Reponse.Favorites.ToggleFavoriteResponse;
import com.sportshop.api.Repository.FavoritesRepository;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritesService {

    private final FavoritesRepository favoritesRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public FavoritesService(FavoritesRepository favoritesRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.favoritesRepository = favoritesRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Toggle favorite: thêm nếu chưa có, xóa nếu đã có
     */
    @Transactional
    public ToggleFavoriteResponse toggleFavorite(ToggleFavoriteRequest request) {
        // Kiểm tra user tồn tại
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        // Kiểm tra product tồn tại
        Products product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));

        // Kiểm tra favorite đã tồn tại chưa
        boolean exists = favoritesRepository.existsByUserAndProduct(user, product);

        if (exists) {
            // Nếu đã có thì xóa
            favoritesRepository.deleteByUserAndProduct(user, product);
            return new ToggleFavoriteResponse(false, "Đã xóa sản phẩm khỏi danh sách yêu thích");
        } else {
            // Nếu chưa có thì thêm mới
            Favorites favorite = new Favorites();
            favorite.setUser(user);
            favorite.setProduct(product);
            favoritesRepository.save(favorite);
            return new ToggleFavoriteResponse(true, "Đã thêm sản phẩm vào danh sách yêu thích");
        }
    }

    /**
     * Lấy danh sách favorites của user
     */
    public List<FavoriteResponse> getUserFavorites(Long userId) {
        // Kiểm tra user tồn tại
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        List<Favorites> favorites = favoritesRepository.findByUser(user);

        return favorites.stream()
                .map(this::convertToFavoriteResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra sản phẩm có trong favorites của user không
     */
    public boolean isProductInFavorites(Long userId, Long productId) {
        // Kiểm tra user tồn tại
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Kiểm tra product tồn tại
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        return favoritesRepository.existsByUserAndProduct(user, product);
    }

    /**
     * Xóa sản phẩm khỏi favorites
     */
    @Transactional
    public void removeFromFavorites(Long userId, Long productId) {
        // Kiểm tra user tồn tại
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Kiểm tra product tồn tại
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        favoritesRepository.deleteByUserAndProduct(user, product);
    }

    /**
     * Convert Favorites entity thành FavoriteResponse
     */
    private FavoriteResponse convertToFavoriteResponse(Favorites favorite) {
        Products product = favorite.getProduct();

        // Lấy ảnh chính của sản phẩm từ imageUrl
        String mainImage = product.getImageUrl();

        return new FavoriteResponse(
                favorite.getId(),
                favorite.getUser().getId(),
                product.getId(),
                product.getName(),
                product.getProductCode(),
                product.getPrice(),
                product.getSale(),
                mainImage,
                null // Có thể thêm trường createdAt vào entity Favorites nếu cần
        );
    }
}