package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.sportshop.api.Domain.Product_images;
import java.util.List;

@Repository
public interface ProductImageRepository
        extends JpaRepository<Product_images, Long>, JpaSpecificationExecutor<Product_images> {

    // Tìm tất cả ảnh của một sản phẩm
    List<Product_images> findByProductId(Long productId);

    // Xóa tất cả ảnh của một sản phẩm
    void deleteByProductId(Long productId);

    // Tìm ảnh theo sản phẩm và màu
    List<Product_images> findByProductIdAndColor(Long productId, String color);
}
