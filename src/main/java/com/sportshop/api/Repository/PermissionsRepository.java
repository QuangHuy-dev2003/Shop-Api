package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sportshop.api.Domain.Permissions;
import java.util.Optional;
import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {

    /**
     * Tìm permission theo tên
     */
    Optional<Permissions> findByName(String name);

    /**
     * Kiểm tra permission có tồn tại theo tên không
     */
    boolean existsByName(String name);

    /**
     * Tìm permission theo tên (không phân biệt hoa thường)
     */
    Optional<Permissions> findByNameIgnoreCase(String name);

    /**
     * Tìm tất cả permission có chứa tên
     */
    List<Permissions> findByNameContainingIgnoreCase(String name);

    /**
     * Tìm permission theo ID và load roles
     */
    @Query("SELECT p FROM Permissions p LEFT JOIN FETCH p.roles WHERE p.id = :id")
    Optional<Permissions> findByIdWithRoles(@Param("id") Long id);

    /**
     * Tìm tất cả permission với roles
     */
    @Query("SELECT p FROM Permissions p LEFT JOIN FETCH p.roles")
    List<Permissions> findAllWithRoles();

    /**
     * Tìm permissions theo danh sách ID
     */
    List<Permissions> findByIdIn(List<Long> ids);
}
