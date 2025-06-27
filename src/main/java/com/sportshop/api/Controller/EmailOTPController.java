package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import com.sportshop.api.Service.EmailOTPService;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Reponse.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class EmailOTPController {

    private final EmailOTPService emailOTPService;

    public EmailOTPController(EmailOTPService emailOTPService) {
        this.emailOTPService = emailOTPService;
    }

    /**
     * Tạo mã OTP cho user
     * 
     * @param userId ID của user cần tạo OTP
     * @return Mã OTP 6 số
     */
    @PostMapping("/otp/generate")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateOTP(@RequestParam Long userId) {
        // TODO: Lấy user từ userId (cần inject UserService)
        // Users user = userService.getUserById(userId);

        // Tạm thời tạo user mock để test
        Users user = new Users();
        user.setId(userId);

        String otpCode = emailOTPService.generateOTP(user);

        Map<String, String> response = new HashMap<>();
        response.put("otpCode", otpCode);
        response.put("message", "Mã OTP đã được tạo thành công. Mã có hiệu lực trong 5 phút.");

        return ResponseEntity.ok(ApiResponse.success(response, "Tạo mã OTP thành công"));
    }

    /**
     * Xác minh mã OTP
     * 
     * @param userId  ID của user
     * @param otpCode Mã OTP cần xác minh
     * @return Kết quả xác minh
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOTP(
            @RequestParam Long userId,
            @RequestParam String otpCode) {

        boolean isValid = emailOTPService.verifyOTP(userId, otpCode);

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        response.put("message", isValid ? "Mã OTP hợp lệ" : "Mã OTP không hợp lệ hoặc đã hết hạn");

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success(response, "Xác minh OTP thành công"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã OTP không hợp lệ"));
        }
    }

    /**
     * Kiểm tra OTP có hợp lệ không (không đánh dấu đã sử dụng)
     * 
     * @param userId  ID của user
     * @param otpCode Mã OTP cần kiểm tra
     * @return Kết quả kiểm tra
     */
    @GetMapping("/otp/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkOTP(
            @RequestParam Long userId,
            @RequestParam String otpCode) {

        boolean isValid = emailOTPService.checkOTPValid(userId, otpCode);

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        response.put("message", isValid ? "Mã OTP còn hiệu lực" : "Mã OTP không hợp lệ hoặc đã hết hạn");

        return ResponseEntity.ok(ApiResponse.success(response, "Kiểm tra OTP thành công"));
    }

    /**
     * Lấy thông tin OTP gần nhất của user
     * 
     * @param userId ID của user
     * @return Thông tin OTP
     */
    @GetMapping("/otp/latest/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLatestOTP(@PathVariable Long userId) {
        var latestOTP = emailOTPService.getLatestOTP(userId);

        Map<String, Object> response = new HashMap<>();
        if (latestOTP != null) {
            response.put("otpId", latestOTP.getId());
            response.put("createdAt", latestOTP.getCreatedAt());
            response.put("expiresAt", latestOTP.getExpiresAt());
            response.put("isUsed", latestOTP.getIsUsed());
            // Không trả về otpCode vì lý do bảo mật
        }

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin OTP thành công"));
    }

    /**
     * Xóa OTP cũ của user
     * 
     * @param userId ID của user
     * @return Kết quả xóa
     */
    @DeleteMapping("/otp/cleanup/{userId}")
    public ResponseEntity<ApiResponse<String>> cleanupOldOTPs(@PathVariable Long userId) {
        emailOTPService.cleanupOldOTPs(userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa OTP cũ thành công"));
    }
}
