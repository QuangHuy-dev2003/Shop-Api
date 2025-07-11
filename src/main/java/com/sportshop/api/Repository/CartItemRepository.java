package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Cart_item;
import com.sportshop.api.Domain.Cart;
import com.sportshop.api.Domain.Products;
import com.sportshop.api.Domain.Product_variants;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<Cart_item, Long> {
    List<Cart_item> findByCart(Cart cart);

    void deleteByCart(Cart cart);

    void deleteByCartAndProduct(Cart cart, Products product);

    void deleteByCartAndProductAndVariant(Cart cart, Products product, Product_variants variant);

    @Modifying
    @Query("DELETE FROM Cart_item c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}