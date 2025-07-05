package com.sportshop.api.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CORSConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép các origin cụ thể (thay đổi theo môi trường)
        List<String> allowedOrigins = Arrays.asList(
                "http://localhost:3000", // React dev
                "http://localhost:3001", // React dev alternative
                "http://localhost:8080", // Vue dev
                "http://localhost:4200" // Angular dev
        // Admin panel
        );
        configuration.setAllowedOrigins(allowedOrigins);

        // Cho phép các HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Cho phép các headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Thời gian cache preflight requests (1 giờ)
        configuration.setMaxAge(3600L);

        // Cho phép expose headers
        configuration.setExposedHeaders(Arrays.asList(
                "Content-Disposition", "Access-Control-Expose-Headers"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}