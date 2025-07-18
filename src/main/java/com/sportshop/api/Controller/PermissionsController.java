package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sportshop.api.Service.PermissionService;
import com.sportshop.api.Domain.Request.Permission.CreatePermissionRequest;
import com.sportshop.api.Domain.Request.Permission.UpdatePermissionRequest;
import com.sportshop.api.Domain.Reponse.Permission.PermissionResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class PermissionsController {

    private final PermissionService permissionService;

    public PermissionsController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Tạo permission mới
     * POST /api/v1/permissions
     */
    @PostMapping("/permissions")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        try {
            PermissionResponse response = permissionService.createPermission(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Tạo quyền hạn thành công",
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
     * Lấy tất cả permissions
     * GET /api/v1/permissions
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        try {
            List<PermissionResponse> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy danh sách quyền hạn thành công",
                    permissions,
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
     * Lấy permissions với phân trang
     * GET /api/v1/permissions/paginated?page=0&size=10
     */
    @GetMapping("/permissions/paginated")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getPermissionsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PermissionResponse> permissionsPage = permissionService.getPermissionsWithPagination(pageable);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy danh sách quyền hạn thành công",
                    permissionsPage,
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
     * Lấy permission theo ID
     * GET /api/v1/permissions/{id}
     */
    @GetMapping("/permissions/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable("id") Long id) {
        try {
            PermissionResponse permission = permissionService.getPermissionById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Lấy thông tin quyền hạn thành công",
                    permission,
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
     * Tìm permissions theo tên
     * GET /api/v1/permissions/search?name=read
     */
    @GetMapping("/permissions/search")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> searchPermissionsByName(
            @RequestParam("name") String name) {
        try {
            List<PermissionResponse> permissions = permissionService.searchPermissionsByName(name);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Tìm kiếm quyền hạn thành công",
                    permissions,
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
     * Cập nhật permission
     * PUT /api/v1/permissions/{id}
     */
    @PutMapping("/permissions/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        try {
            PermissionResponse response = permissionService.updatePermission(id, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Cập nhật quyền hạn thành công",
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
     * Xóa permission
     * DELETE /api/v1/permissions/{id}
     */
    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<ApiResponse<String>> deletePermission(@PathVariable("id") Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Xóa quyền hạn thành công",
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

}
