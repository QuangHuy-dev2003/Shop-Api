package com.sportshop.api.Domain.Request.Auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private Long userId;
    private String currentPassword;
    private String newPassword;
}