package com.sportshop.api.Repository;

import com.sportshop.api.Domain.UserUsedDiscountCode;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Discounts;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserUsedDiscountCodeRepository extends JpaRepository<UserUsedDiscountCode, Long> {
    Optional<UserUsedDiscountCode> findByUserAndDiscount(Users user, Discounts discount);

    long countByUserAndDiscount(Users user, Discounts discount);
}