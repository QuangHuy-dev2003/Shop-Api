package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Favorites;

@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Long> {

    @Modifying
    @Query("DELETE FROM Favorites f WHERE f.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
