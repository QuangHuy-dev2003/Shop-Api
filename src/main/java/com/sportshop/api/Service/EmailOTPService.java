package com.sportshop.api.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.api.Domain.Email_otp;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Repository.EmailOTPRepository;

@Service
public class EmailOTPService {

    private final EmailOTPRepository emailOTPRepository;

    public EmailOTPService(EmailOTPRepository emailOTPRepository) {
        this.emailOTPRepository = emailOTPRepository;
    }

    /**
     * Tạo mã OTP 6 số cho user
     * 
     * @param user User cần tạo OTP
     * @return Mã OTP 6 số
     */
    @Transactional
    public String generateOTP(Users user) {
        // Tạo mã OTP 6 số ngẫu nhiên
        String otpCode = generateRandomOTP();

        // Vô hiệu hóa tất cả OTP cũ của user này
        invalidateOldOTPs(user.getId());

        // Tạo OTP mới
        Email_otp emailOTP = new Email_otp();
        emailOTP.setUser(user);
        emailOTP.setOtpCode(otpCode);
        // Thời gian hết hạn được set tự động trong @PrePersist (5 phút)

        emailOTPRepository.save(emailOTP);

        return otpCode;
    }

    /**
     * Xác minh mã OTP
     * 
     * @param userId  ID của user
     * @param otpCode Mã OTP cần xác minh
     * @return true nếu OTP hợp lệ, false nếu không hợp lệ
     */
    @Transactional
    public boolean verifyOTP(Long userId, String otpCode) {
        // Tìm OTP chưa sử dụng và chưa hết hạn
        Optional<Email_otp> otpOptional = emailOTPRepository.findByUserIdAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
                userId, otpCode, LocalDateTime.now());

        if (otpOptional.isPresent()) {
            Email_otp otp = otpOptional.get();

            // Đánh dấu OTP đã sử dụng
            otp.setIsUsed(true);
            emailOTPRepository.save(otp);

            return true;
        }

        return false;
    }

    /**
     * Kiểm tra OTP có hợp lệ không (không đánh dấu đã sử dụng)
     * 
     * @param userId  ID của user
     * @param otpCode Mã OTP cần kiểm tra
     * @return true nếu OTP hợp lệ, false nếu không hợp lệ
     */
    public boolean checkOTPValid(Long userId, String otpCode) {
        return emailOTPRepository.findByUserIdAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
                userId, otpCode, LocalDateTime.now()).isPresent();
    }

    /**
     * Lấy thông tin OTP gần nhất của user
     * 
     * @param userId ID của user
     * @return Thông tin OTP hoặc null nếu không có
     */
    public Email_otp getLatestOTP(Long userId) {
        return emailOTPRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Xóa tất cả OTP cũ của user
     * 
     * @param userId ID của user
     */
    @Transactional
    public void cleanupOldOTPs(Long userId) {
        // Xóa tất cả OTP đã hết hạn hoặc đã sử dụng
        emailOTPRepository.deleteByUserIdAndExpiresAtBeforeOrIsUsedTrue(userId, LocalDateTime.now());
    }

    /**
     * Tạo mã OTP 6 số ngẫu nhiên
     * 
     * @return Mã OTP 6 số
     */
    private String generateRandomOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10)); // Số từ 0-9
        }

        return otp.toString();
    }

    /**
     * Vô hiệu hóa tất cả OTP cũ của user
     * 
     * @param userId ID của user
     */
    private void invalidateOldOTPs(Long userId) {
        // Có thể implement logic vô hiệu hóa OTP cũ nếu cần
        // Hiện tại chỉ xóa OTP đã hết hạn
        cleanupOldOTPs(userId);
    }
}
