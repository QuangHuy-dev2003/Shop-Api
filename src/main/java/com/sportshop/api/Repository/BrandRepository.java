package com.sportshop.api.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sportshop.api.Domain.Brand;
import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    // Tìm brand theo tên
    Optional<Brand> findByName(String name);

    // Tìm brand đang active
    List<Brand> findByIsActiveTrue();
}