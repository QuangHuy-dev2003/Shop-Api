package com.sportshop.api.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtConfig {
    // Lấy các giá trị từ application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.refresh-token.max-per-user}")
    private int maxRefreshTokenPerUser;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public int getMaxRefreshTokenPerUser() {
        return maxRefreshTokenPerUser;
    }
}