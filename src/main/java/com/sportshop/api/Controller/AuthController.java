package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.sportshop.api.Service.AuthService;
import com.sportshop.api.Domain.Request.Auth.RegisterRequest;
import com.sportshop.api.Domain.Request.Auth.LoginRequest;
import com.sportshop.api.Domain.Request.Auth.VerifyOTPRequest;
import com.sportshop.api.Domain.Reponse.Auth.AuthResponse;
import com.sportshop.api.Domain.Reponse.Auth.RegisterResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Đăng ký tài khoản mới
     * POST /api/auth/register
     */
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Đăng ký thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Đăng nhập
     * POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Đăng nhập thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Xác thực OTP
     * POST /api/auth/verify-otp
     */
    @PostMapping("/auth/verify-otp")
    public ResponseEntity<ApiResponse<RegisterResponse>> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            RegisterResponse response = authService.verifyOTP(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Xác thực tài khoản thành công! Bạn có thể đăng nhập.",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Gửi lại OTP
     * POST /api/auth/resend-otp
     */
    @PostMapping("/auth/resend-otp")
    public ResponseEntity<ApiResponse<RegisterResponse>> resendOTP(@RequestParam("email") String email) {
        try {
            RegisterResponse response = authService.resendOTP(email);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Đã gửi lại mã OTP",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Refresh token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/auth/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        try {
            System.out.println("Refresh token request received: " + refreshToken.substring(0, 20) + "...");
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Refresh token thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("Refresh token error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Đăng xuất
     * POST /api/auth/logout
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String refreshToken) {
        try {
            authService.logout(refreshToken);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Đăng xuất thành công",
                    null,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     * GET /api/auth/check-status
     */
    @GetMapping("/auth/check-status")
    public ResponseEntity<ApiResponse<String>> checkStatus() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "API hoạt động bình thường",
                "Auth service is running",
                LocalDateTime.now()));
    }

    /**
     * Gửi OTP quên mật khẩu
     * POST /api/auth/forgot-password/request
     */
    @PostMapping("/auth/forgot-password/request")
    public ResponseEntity<ApiResponse<RegisterResponse>> forgotPasswordRequest(@RequestParam("email") String email) {
        try {
            RegisterResponse response = authService.forgotPasswordRequest(email);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Đã gửi mã OTP đặt lại mật khẩu",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Cập nhật mật khẩu theo email (không cần OTP)
     * POST /api/auth/update-password
     */
    @PostMapping("/auth/update-password")
    public ResponseEntity<ApiResponse<RegisterResponse>> updatePassword(@RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {
        try {
            RegisterResponse response = authService.updatePasswordByEmail(email, newPassword);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Cập nhật mật khẩu thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Kích hoạt tài khoản admin (đặc biệt cho tài khoản được tạo trực tiếp trong
     * DB)
     * POST /api/auth/activate-admin
     */
    @PostMapping("/auth/activate-admin")
    public ResponseEntity<ApiResponse<RegisterResponse>> activateAdminAccount(@RequestParam String email) {
        try {
            RegisterResponse response = authService.activateAdminAccount(email);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Kích hoạt tài khoản admin thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Lấy URL để đăng nhập Google OAuth2
     */
    @GetMapping("/auth/google/login")
    public ResponseEntity<ApiResponse<String>> getGoogleLoginUrl() {
        String googleLoginUrl = "http://localhost:8080/oauth2/authorization/google";
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Redirect to Google OAuth2",
                googleLoginUrl,
                LocalDateTime.now()));
    }

    /**
     * Endpoint trả về thông tin user sau khi đăng nhập Google OAuth2 thành công
     */
    @GetMapping("/auth/google-success")
    public ResponseEntity<?> googleLoginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> attributes = principal.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        // Xử lý logic của bạn ở đây (tạo token, trả về user info, ...)
        return ResponseEntity.ok(attributes);
    }
}