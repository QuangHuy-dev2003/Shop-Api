package com.sportshop.api.Config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Tiện ích xử lý JWT: sinh token, xác thực, trích xuất thông tin.
 */
@Component
public class JwtUtil {
    private final JwtConfig jwtConfig;
    private Key key;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // Khởi tạo key từ secret khi bean được tạo
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getJwtSecret().getBytes());
        System.out.println("JWT SECRET ACTUALLY USED: " + jwtConfig.getJwtSecret());
    }

    /**
     * Sinh access token cho user
     */
    public String generateAccessToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Sinh refresh token cho user
     */
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy thông tin (subject/email) từ token
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Xác thực token hợp lệ không (chữ ký, hạn, ...)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token hết hạn
            return false;
        } catch (JwtException e) {
            // Token không hợp lệ
            return false;
        }
    }

    /**
     * Trích xuất claim bất kỳ từ token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Trích xuất toàn bộ claims từ token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}