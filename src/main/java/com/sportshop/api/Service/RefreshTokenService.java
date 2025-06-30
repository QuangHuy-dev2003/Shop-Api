package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.sportshop.api.Domain.RefreshToken;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:604800}") // 7 days default
    private long refreshTokenExpirationMs;

    @Value("${jwt.refresh-token.max-per-user:5}") // Max 5 tokens per user
    private int maxTokensPerUser;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Tạo refresh token mới cho user
     */
    @Transactional
    public RefreshToken createRefreshToken(Users user) {
        // Kiểm tra số lượng token hiện tại của user
        long currentTokenCount = refreshTokenRepository.countValidTokensByUserId(user.getId(), LocalDateTime.now());

        if (currentTokenCount >= maxTokensPerUser) {
            // Revoke token cũ nhất nếu vượt quá giới hạn
            revokeOldestToken(user.getId());
        }

        // Tạo token mới
        String token = generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Xác thực refresh token
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findValidTokenByToken(token, LocalDateTime.now());
    }

    /**
     * Revoke refresh token
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.revokeTokenByToken(token);
    }

    /**
     * Revoke tất cả token của user
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllTokensByUserId(userId);
    }

    /**
     * Lấy danh sách token hợp lệ của user
     */
    public List<RefreshToken> getUserValidTokens(Long userId) {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
    }

    /**
     * Xóa tất cả token đã hết hạn
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Tạo refresh token string ngẫu nhiên
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /**
     * Revoke token cũ nhất của user
     */
    @Transactional
    private void revokeOldestToken(Long userId) {
        List<RefreshToken> userTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
        if (!userTokens.isEmpty()) {
            // Sắp xếp theo thời gian tạo và revoke token cũ nhất
            userTokens.sort((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()));
            refreshTokenRepository.revokeTokenByToken(userTokens.get(0).getToken());
        }
    }
}