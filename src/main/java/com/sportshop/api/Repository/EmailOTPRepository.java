package com.sportshop.api.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportshop.api.Domain.Email_otp;

@Repository
public interface EmailOTPRepository extends JpaRepository<Email_otp, Long> {

    /**
     * Tìm OTP theo userId, otpCode, chưa sử dụng và chưa hết hạn
     */
    @Query("SELECT e FROM Email_otp e WHERE e.user.id = :userId AND e.otpCode = :otpCode AND e.isUsed = false AND e.expiresAt > :now")
    Optional<Email_otp> findByUserIdAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
            @Param("userId") Long userId,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now);

    /**
     * Lấy OTP gần nhất của user
     */
    @Query("SELECT e FROM Email_otp e WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    Email_otp findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Xóa OTP đã hết hạn hoặc đã sử dụng
     */
    @Modifying
    @Query("DELETE FROM Email_otp e WHERE e.user.id = :userId AND (e.expiresAt < :now OR e.isUsed = true)")
    void deleteByUserIdAndExpiresAtBeforeOrIsUsedTrue(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
