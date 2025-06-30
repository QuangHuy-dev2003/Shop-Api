package com.sportshop.api.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sportshop.api.Domain.Permissions;
import com.sportshop.api.Domain.Request.Permission.CreatePermissionRequest;
import com.sportshop.api.Domain.Request.Permission.UpdatePermissionRequest;
import com.sportshop.api.Domain.Reponse.Permission.PermissionResponse;
import com.sportshop.api.Repository.PermissionsRepository;

@Service
public class PermissionService {
    private final PermissionsRepository permissionsRepository;

    public PermissionService(PermissionsRepository permissionsRepository) {
        this.permissionsRepository = permissionsRepository;
    }

    /**
     * Tạo permission mới
     */
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        // Kiểm tra tên permission đã tồn tại chưa
        if (permissionsRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên quyền hạn đã tồn tại trong hệ thống");
        }

        // Tạo permission mới
        Permissions permission = new Permissions();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setApiPath(request.getApiPath());
        permission.setMethod(request.getMethod());
        permission.setModule(request.getModule());
        permission.setActive(request.getActive() != null ? request.getActive() : true);

        Permissions savedPermission = permissionsRepository.save(permission);
        return convertToPermissionResponse(savedPermission);
    }

    /**
     * Lấy tất cả permissions
     */
    public List<PermissionResponse> getAllPermissions() {
        List<Permissions> permissions = permissionsRepository.findAll();
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy permissions với phân trang
     */
    public Page<PermissionResponse> getPermissionsWithPagination(Pageable pageable) {
        Page<Permissions> permissionsPage = permissionsRepository.findAll(pageable);
        return permissionsPage.map(this::convertToPermissionResponse);
    }

    /**
     * Lấy permission theo ID
     */
    public PermissionResponse getPermissionById(Long id) {
        Permissions permission = permissionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền hạn với ID: " + id));
        return convertToPermissionResponse(permission);
    }

    /**
     * Tìm permissions theo tên
     */
    public List<PermissionResponse> searchPermissionsByName(String name) {
        List<Permissions> permissions = permissionsRepository.findByNameContainingIgnoreCase(name);
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật permission
     */
    @Transactional
    public PermissionResponse updatePermission(Long id, UpdatePermissionRequest request) {
        Permissions permission = permissionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền hạn với ID: " + id));

        // Kiểm tra tên mới có trùng không (nếu có thay đổi)
        if (request.getName() != null && !request.getName().equals(permission.getName())) {
            if (permissionsRepository.existsByName(request.getName())) {
                throw new RuntimeException("Tên quyền hạn đã tồn tại trong hệ thống");
            }
            permission.setName(request.getName());
        }

        // Cập nhật description
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }

        // Cập nhật apiPath
        if (request.getApiPath() != null) {
            permission.setApiPath(request.getApiPath());
        }

        // Cập nhật method
        if (request.getMethod() != null) {
            permission.setMethod(request.getMethod());
        }

        // Cập nhật module
        if (request.getModule() != null) {
            permission.setModule(request.getModule());
        }

        // Cập nhật active
        if (request.getActive() != null) {
            permission.setActive(request.getActive());
        }

        Permissions updatedPermission = permissionsRepository.save(permission);
        return convertToPermissionResponse(updatedPermission);
    }

    /**
     * Xóa permission
     */
    @Transactional
    public void deletePermission(Long id) {
        Permissions permission = permissionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền hạn với ID: " + id));

        // Xóa liên kết với các role (bảng phụ role_permissions)
        if (permission.getRoles() != null) {
            permission.getRoles().forEach(role -> role.getPermissions().remove(permission));
            permission.getRoles().clear();
        }
        permissionsRepository.save(permission); // Cập nhật lại để xóa liên kết

        permissionsRepository.delete(permission);
    }

    /**
     * Lấy permissions theo danh sách ID
     */
    public List<PermissionResponse> getPermissionsByIds(List<Long> ids) {
        List<Permissions> permissions = permissionsRepository.findByIdIn(ids);
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Permissions thành PermissionResponse
     */
    private PermissionResponse convertToPermissionResponse(Permissions permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getActive());
    }
}
