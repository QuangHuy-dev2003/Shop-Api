package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sportshop.api.Domain.Role;
import com.sportshop.api.Domain.Permissions;
import com.sportshop.api.Domain.Request.Role.CreateRoleRequest;
import com.sportshop.api.Domain.Request.Role.UpdateRoleRequest;
import com.sportshop.api.Domain.Reponse.Role.RoleResponse;
import com.sportshop.api.Repository.RoleRepository;
import com.sportshop.api.Repository.PermissionsRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionsRepository permissionsRepository;

    public RoleService(RoleRepository roleRepository, PermissionsRepository permissionsRepository) {
        this.roleRepository = roleRepository;
        this.permissionsRepository = permissionsRepository;
    }

    /**
     * Tạo role mới
     */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        // Kiểm tra tên role đã tồn tại chưa
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên vai trò đã tồn tại trong hệ thống");
        }

        // Tạo role mới
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Gán permissions nếu có
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permissions> permissions = permissionsRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return convertToRoleResponse(savedRole);
    }

    /**
     * Lấy tất cả roles
     */
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAllWithPermissions();
        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy roles với phân trang
     */
    public Page<RoleResponse> getRolesWithPagination(Pageable pageable) {
        Page<Role> rolesPage = roleRepository.findAll(pageable);
        return rolesPage.map(this::convertToRoleResponse);
    }

    /**
     * Lấy role theo ID
     */
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));
        return convertToRoleResponse(role);
    }

    /**
     * Tìm roles theo tên
     */
    public List<RoleResponse> searchRolesByName(String name) {
        List<Role> roles = roleRepository.findByNameContainingIgnoreCase(name);
        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật role
     */
    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));

        // Kiểm tra tên mới có trùng không (nếu có thay đổi)
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new RuntimeException("Tên vai trò đã tồn tại trong hệ thống");
            }
            role.setName(request.getName());
        }

        // Cập nhật description
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        // Cập nhật permissions
        if (request.getPermissionIds() != null) {
            List<Permissions> permissions = permissionsRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return convertToRoleResponse(updatedRole);
    }

    /**
     * Xóa role
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));

        // Xóa liên kết với các permission (bảng phụ role_permissions)
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(permission -> permission.getRoles().remove(role));
            role.getPermissions().clear();
        }
        roleRepository.save(role); // Cập nhật lại để xóa liên kết

        roleRepository.delete(role);
    }

    /**
     * Gán permissions cho role
     */
    @Transactional
    public RoleResponse assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new RuntimeException("Danh sách quyền hạn không được để trống");
        }

        // Kiểm tra từng ID permission có tồn tại không
        List<Permissions> permissions = permissionsRepository.findByIdIn(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            // Tìm ra những ID không tồn tại
            List<Long> existingIds = permissions.stream()
                    .map(Permissions::getId)
                    .collect(Collectors.toList());
            List<Long> nonExistingIds = permissionIds.stream()
                    .filter(id -> !existingIds.contains(id))
                    .collect(Collectors.toList());

            throw new RuntimeException("Không tìm thấy quyền hạn với các ID: " + nonExistingIds);
        }

        // Gán permissions cho role
        role.setPermissions(permissions);

        Role updatedRole = roleRepository.save(role);
        return convertToRoleResponse(updatedRole);
    }

    /**
     * Xóa permissions khỏi role
     */
    @Transactional
    public RoleResponse removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new RuntimeException("Danh sách quyền hạn không được để trống");
        }

        List<Permissions> currentPermissions = role.getPermissions();

        // Kiểm tra từng ID permission có tồn tại không
        List<Permissions> permissionsToRemove = permissionsRepository.findByIdIn(permissionIds);
        if (permissionsToRemove.size() != permissionIds.size()) {
            // Tìm ra những ID không tồn tại
            List<Long> existingIds = permissionsToRemove.stream()
                    .map(Permissions::getId)
                    .collect(Collectors.toList());
            List<Long> nonExistingIds = permissionIds.stream()
                    .filter(id -> !existingIds.contains(id))
                    .collect(Collectors.toList());

            throw new RuntimeException("Không tìm thấy quyền hạn với các ID: " + nonExistingIds);
        }

        // Lọc ra các permission thực sự đang có trong role
        List<Permissions> actuallyRemovable = permissionsToRemove.stream()
                .filter(currentPermissions::contains)
                .collect(Collectors.toList());

        if (actuallyRemovable.isEmpty()) {
            throw new RuntimeException("Không có quyền hạn nào trong danh sách đang thuộc vai trò này");
        }

        currentPermissions.removeAll(actuallyRemovable);
        role.setPermissions(currentPermissions);

        Role updatedRole = roleRepository.save(role);
        return convertToRoleResponse(updatedRole);
    }

    /**
     * Chuyển đổi Role thành RoleResponse
     */
    private RoleResponse convertToRoleResponse(Role role) {
        List<RoleResponse.PermissionInfo> permissionInfos = null;

        if (role.getPermissions() != null) {
            permissionInfos = role.getPermissions().stream()
                    .map(permission -> new RoleResponse.PermissionInfo(
                            permission.getId(),
                            permission.getName(),
                            permission.getDescription()))
                    .collect(Collectors.toList());
        }

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissionInfos);
    }
}
