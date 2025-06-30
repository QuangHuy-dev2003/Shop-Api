package com.sportshop.api.Domain.Request.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "Tên quyền hạn không được để trống")
    @Size(min = 2, max = 100, message = "Tên quyền hạn phải từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String description;

    @Size(max = 255, message = "API path không được quá 255 ký tự")
    private String apiPath;

    @Size(max = 10, message = "Method không được quá 10 ký tự")
    private String method;

    @Size(max = 100, message = "Module không được quá 100 ký tự")
    private String module;

    private Boolean active = true;
}