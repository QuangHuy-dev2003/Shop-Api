package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Users;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * Tìm user theo email
     */
    Optional<Users> findByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);

    /**
     * Tìm user theo email và provider
     */
    Optional<Users> findByEmailAndProvider(String email, Users.Provider provider);

    /**
     * Tìm user theo phone
     */
    Optional<Users> findByPhone(String phone);

    /**
     * Kiểm tra phone đã tồn tại chưa
     */
    boolean existsByPhone(String phone);

    /**
     * Tìm user theo role
     */
    @Query("SELECT u FROM Users u WHERE u.roleId = :roleId")
    java.util.List<Users> findByRoleId(@Param("roleId") Long roleId);
}
