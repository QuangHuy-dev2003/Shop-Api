package com.sportshop.api.Security;

import com.sportshop.api.Config.JwtUtil;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Repository.RefreshTokenRepository;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.RefreshToken;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Domain.Reponse.Auth.AuthResponse;
import com.sportshop.api.Config.JwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    public CustomOAuth2SuccessHandler(JwtUtil jwtUtil, UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtConfig jwtConfig, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtConfig = jwtConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        String avatar = (String) oAuth2User.getAttributes().get("picture");

        // Lấy user từ database (đã được lưu trong CustomOAuth2UserService)
        Users user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User not found after OAuth2 login");
            return;
        }

        // Tạo JWT tokens
        String accessToken = jwtUtil.generateAccessToken(email, user.getRoleId(), user.getFullName());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Lưu refresh token vào database
        saveRefreshToken(user, refreshToken);

        // Tạo user info response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender().toString(),
                user.getAvatar(),
                user.getRoleId(),
                user.getFirstLogin());

        // Tạo auth response
        AuthResponse authResponse = new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtConfig.getAccessTokenExpirationMs(),
                userInfo);

        // Tạo API response wrapper
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(
                true,
                "Đăng nhập Google thành công",
                authResponse,
                LocalDateTime.now());

        // Redirect về frontend với response data
        String responseJson = objectMapper.writeValueAsString(apiResponse);
        String encodedResponse = URLEncoder.encode(responseJson, StandardCharsets.UTF_8);

        String redirectUrl = String.format("http://localhost:5174/oauth-callback?response=%s",
                encodedResponse);

        response.sendRedirect(redirectUrl);
    }

    private void saveRefreshToken(Users user, String refreshToken) {
        // Xóa refresh tokens cũ nếu vượt quá giới hạn
        List<RefreshToken> existingTokens = refreshTokenRepository.findValidTokensByUserId(user.getId(),
                LocalDateTime.now());
        if (existingTokens.size() >= jwtConfig.getMaxRefreshTokenPerUser()) {
            // Xóa token cũ nhất
            RefreshToken oldestToken = existingTokens.stream()
                    .min((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .orElse(null);
            if (oldestToken != null) {
                refreshTokenRepository.delete(oldestToken);
            }
        }

        // Tạo refresh token mới
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(refreshToken);
        newRefreshToken.setUser(user);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpirationMs() / 1000));
        newRefreshToken.setIsRevoked(false);
        refreshTokenRepository.save(newRefreshToken);
    }

}