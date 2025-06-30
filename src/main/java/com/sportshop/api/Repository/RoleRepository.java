package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sportshop.api.Domain.Role;
import java.util.Optional;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm role theo tên
     */
    Optional<Role> findByName(String name);

    /**
     * Kiểm tra role có tồn tại theo tên không
     */
    boolean existsByName(String name);

    /**
     * Tìm role theo tên (không phân biệt hoa thường)
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * Tìm tất cả role có chứa tên
     */
    List<Role> findByNameContainingIgnoreCase(String name);

    /**
     * Tìm role theo ID và load permissions
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Tìm tất cả role với permissions
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
}
