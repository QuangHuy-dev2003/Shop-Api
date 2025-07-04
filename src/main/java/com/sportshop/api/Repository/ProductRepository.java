package com.sportshop.api.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Products;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {
    Optional<Products> findByProductCode(String productCode);
}
