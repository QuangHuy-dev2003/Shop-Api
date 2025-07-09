package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Order_items;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.sportshop.api.Domain.Orders;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemsRepository extends JpaRepository<Order_items, Long> {
    List<Order_items> findByOrder(Orders order);

    @Modifying
    @Query("UPDATE Order_items o SET o.product = null WHERE o.product.id = :productId")
    void setProductIdNull(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Order_items o SET o.variant = null WHERE o.variant.id = :variantId")
    void setVariantIdNull(@Param("variantId") Long variantId);
}