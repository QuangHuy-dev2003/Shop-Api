package com.sportshop.api.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("statusCode", HttpStatus.FORBIDDEN.value());
        res.put("timestamp", System.currentTimeMillis());
        res.put("error", accessDeniedException.getMessage());
        res.put("message", "Không có quyền truy cập tài nguyên này");
        res.put("path", request.getRequestURI());

        mapper.writeValue(response.getWriter(), res);
    }
}