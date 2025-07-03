package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Shipping_addresses;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<Shipping_addresses, Long> {

    /**
     * Tìm tất cả địa chỉ của user
     */
    List<Shipping_addresses> findByUserId(Long userId);

    /**
     * Tìm địa chỉ mặc định của user
     */
    Optional<Shipping_addresses> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Đếm số địa chỉ của user
     */
    long countByUserId(Long userId);

    /**
     * Reset tất cả địa chỉ về không mặc định
     */
    @Modifying
    @Query("UPDATE Shipping_addresses sa SET sa.isDefault = false WHERE sa.user.id = :userId")
    void resetDefaultAddressByUserId(@Param("userId") Long userId);

    /**
     * Xóa tất cả địa chỉ của user
     */
    @Modifying
    @Query("DELETE FROM Shipping_addresses sa WHERE sa.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}