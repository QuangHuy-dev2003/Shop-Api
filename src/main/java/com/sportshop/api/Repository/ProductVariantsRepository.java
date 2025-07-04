package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sportshop.api.Domain.Product_variants;
import java.util.List;

@Repository
public interface ProductVariantsRepository extends JpaRepository<Product_variants, Long> {
    // Tìm tất cả variants của một sản phẩm
    List<Product_variants> findByProductId(Long productId);

    // Tìm variant theo sản phẩm và size
    Product_variants findByProductIdAndSize(Long productId, Product_variants.Size size);

    // Xóa tất cả variants của một sản phẩm
    void deleteByProductId(Long productId);

    // Tìm variant theo sản phẩm, màu và size
    Product_variants findByProductIdAndColorAndSize(Long productId, String color, Product_variants.Size size);
}
