package com.sportshop.api.Domain.Request.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    private Long roleId;

    private Boolean firstLogin;

    private String avatar;

    private String gender;

    // Thông tin địa chỉ (không bắt buộc)
    private String addressLine;
    private String ward;
    private String district;
    private String province;
}