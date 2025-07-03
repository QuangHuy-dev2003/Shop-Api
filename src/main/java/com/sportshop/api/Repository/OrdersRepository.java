package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}