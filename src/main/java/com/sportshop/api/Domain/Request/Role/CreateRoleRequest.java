package com.sportshop.api.Domain.Request.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(min = 2, max = 100, message = "Tên vai trò phải từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String description;

    private List<Long> permissionIds;
}