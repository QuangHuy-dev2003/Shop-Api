package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Payments;
import com.sportshop.api.Domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    List<Payments> findByOrder(Orders order);
}