package com.sportshop.api.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Products;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {
    Optional<Products> findByProductCode(String productCode);

    // Lấy tất cả sản phẩm có sale (sale > 0 và isActive = true)
    @Query("SELECT p FROM Products p WHERE p.sale > 0 AND p.isActive = true ORDER BY p.id DESC")
    List<Products> findAllSaleProducts();

    // Lấy top 10 sản phẩm sale theo id giảm dần
    @Query(value = "SELECT * FROM products p WHERE p.sale > 0 AND p.is_active = true ORDER BY p.id DESC LIMIT 10", nativeQuery = true)
    List<Products> findTop10SaleProducts();

    // Lấy top 10 sản phẩm mới theo category name không thuộc sale, sắp xếp theo ID
    // giảm dần
    @Query("SELECT p FROM Products p JOIN p.category c WHERE c.name = :categoryName AND (p.sale IS NULL OR p.sale = 0) AND p.isActive = true ORDER BY p.id DESC")
    List<Products> findTop10NewProductsByCategory(@Param("categoryName") String categoryName);

    // Search sản phẩm theo tên (LIKE, ignore case, chỉ lấy sản phẩm active, limit
    // 5)
    @Query(value = "SELECT * FROM products p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.is_active = true LIMIT 5", nativeQuery = true)
    List<Products> searchActiveProductsByName(@Param("keyword") String keyword);
}
