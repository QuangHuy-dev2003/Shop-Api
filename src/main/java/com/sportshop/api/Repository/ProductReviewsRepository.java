package com.sportshop.api.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Product_reviews;
import com.sportshop.api.Domain.Products;

@Repository
public interface ProductReviewsRepository extends JpaRepository<Product_reviews, Long> {
    List<Product_reviews> findByProduct(Products product);

    @Modifying
    @Query("DELETE FROM Product_reviews r WHERE r.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

}
