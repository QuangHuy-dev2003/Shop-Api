package com.sportshop.api.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Discounts;

@Repository
public interface DiscountsRepository extends JpaRepository<Discounts, Long> {
    Optional<Discounts> findByCode(String code);

}
