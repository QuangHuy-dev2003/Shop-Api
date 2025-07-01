package com.sportshop.api.Repository;

import com.sportshop.api.Domain.Cart;
import com.sportshop.api.Domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(Users user);

    void deleteByUser(Users user);
}