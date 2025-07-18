package com.sportshop.api.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/api/v1/auth/**",
                "/api/v1/otp/**",
                "/error",
                "/",
                "/login**",
                "/oauth2/**",
                "/api/v1/cart/**",
                "/api/v1/favorites/**",
                "/api/v1/shipping-addresses/**",
                "/api/v1/orders/place-order",
                "/api/v1/orders/user/{userId}",
                "/api/v1/orders/{orderId}/cancel/user",
                "/api/v1/orders/{orderId}",
                "/api/v1/vnpay/**",
                "/api/v1/products/**",
                "/api/v1/categories/**",
                "/api/v1/brands/**",
                "/api/v1/discounts/**",
                "/api/v1/download-excel-template",
                "/api/v1/users/*/avatar" // POST, DELETE avatar public
        };
        registry.addInterceptor(permissionInterceptor)
                .excludePathPatterns(whiteList);
    }
}