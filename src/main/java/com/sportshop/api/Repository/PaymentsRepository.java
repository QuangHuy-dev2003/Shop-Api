package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
}