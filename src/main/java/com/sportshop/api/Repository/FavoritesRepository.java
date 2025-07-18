package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Favorites;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Products;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Long> {

    /**
     * Tìm favorite theo user và product
     */
    Optional<Favorites> findByUserAndProduct(Users user, Products product);

    /**
     * Kiểm tra favorite đã tồn tại chưa
     */
    boolean existsByUserAndProduct(Users user, Products product);

    /**
     * Lấy danh sách favorites của user
     */
    List<Favorites> findByUser(Users user);

    /**
     * Xóa favorite theo user và product
     */
    void deleteByUserAndProduct(Users user, Products product);

    @Modifying
    @Query("DELETE FROM Favorites f WHERE f.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
