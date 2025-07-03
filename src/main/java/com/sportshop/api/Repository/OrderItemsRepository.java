package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Order_items;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.sportshop.api.Domain.Orders;

public interface OrderItemsRepository extends JpaRepository<Order_items, Long> {
    List<Order_items> findByOrder(Orders order);
}