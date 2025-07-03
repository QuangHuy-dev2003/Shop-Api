package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Order_items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<Order_items, Long> {
}