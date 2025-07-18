package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sportshop.api.Service.RoleService;
import com.sportshop.api.Domain.Request.Role.CreateRoleRequest;
import com.sportshop.api.Domain.Request.Role.UpdateRoleRequest;
import com.sportshop.api.Domain.Reponse.Role.RoleResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Tạo role mới
     * POST /api/v1/roles
     */
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleResponse response = roleService.createRole(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Tạo vai trò thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Lấy tất cả roles
     * GET /api/v1/roles
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        try {
            List<RoleResponse> roles = roleService.getAllRoles();
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy danh sách vai trò thành công",
                    roles,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Lấy roles với phân trang
     * GET /api/v1/roles/paginated?page=0&size=10
     */
    @GetMapping("/roles/paginated")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getRolesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RoleResponse> rolesPage = roleService.getRolesWithPagination(pageable);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy danh sách vai trò thành công",
                    rolesPage,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Lấy role theo ID
     * GET /api/v1/roles/{id}
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable("id") Long id) {
        try {
            RoleResponse role = roleService.getRoleById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy thông tin vai trò thành công",
                    role,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Tìm roles theo tên
     * GET /api/v1/roles/search?name=admin
     */
    @GetMapping("/roles/search")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> searchRolesByName(@RequestParam("name") String name) {
        try {
            List<RoleResponse> roles = roleService.searchRolesByName(name);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Tìm kiếm vai trò thành công",
                    roles,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Cập nhật role
     * PUT /api/v1/roles/{id}
     */
    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            RoleResponse response = roleService.updateRole(id, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Cập nhật vai trò thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Xóa role
     * DELETE /api/v1/roles/{id}
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable("id") Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Xóa vai trò thành công",
                    null,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Gán permissions cho role
     * POST /api/v1/roles/{id}/permissions
     */
    @PostMapping("/roles/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissionsToRole(
            @PathVariable("id") Long id,
            @RequestBody List<Long> permissionIds) {
        try {
            RoleResponse response = roleService.assignPermissionsToRole(id, permissionIds);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Gán quyền hạn cho vai trò thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }

    /**
     * Xóa permissions khỏi role
     * DELETE /api/v1/roles/{id}/permissions
     */
    @DeleteMapping("/roles/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissionsFromRole(
            @PathVariable("id") Long id,
            @RequestBody List<Long> permissionIds) {
        try {
            RoleResponse response = roleService.removePermissionsFromRole(id, permissionIds);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Xóa quyền hạn khỏi vai trò thành công",
                    response,
                    LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()));
        }
    }
}
