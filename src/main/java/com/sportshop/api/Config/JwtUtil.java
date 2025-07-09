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
import java.util.HashMap;
import java.util.Map;
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
        System.out.println("JWT SECRET LENGTH: " + jwtConfig.getJwtSecret().length());
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
     * Sinh access token cho user với role_id và name
     */
    public String generateAccessToken(String subject, Long roleId, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", subject); // Thêm email vào claims
        claims.put("role_id", roleId);
        claims.put("name", name);

        return Jwts.builder()
                .setClaims(claims)
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
     * Lấy role_id từ token
     */
    public Long extractRoleId(String token) {
        return extractClaim(token, claims -> {
            Object roleIdObj = claims.get("role_id");
            if (roleIdObj instanceof Integer) {
                return ((Integer) roleIdObj).longValue();
            }
            return (Long) roleIdObj;
        });
    }

    /**
     * Lấy name từ token
     */
    public String extractName(String token) {
        return extractClaim(token, claims -> (String) claims.get("name"));
    }

    /**
     * Lấy email từ token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> (String) claims.get("email"));
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
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            // Token không hợp lệ
            System.out.println("Token invalid: " + e.getMessage());
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