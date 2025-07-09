package com.sportshop.api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportshop.api.Service.ShippingAddressService;
import com.sportshop.api.Service.UserService;
import com.sportshop.api.Domain.Request.ShippingAddress.CreateAddressRequest;
import com.sportshop.api.Domain.Request.ShippingAddress.UpdateAddressRequest;
import com.sportshop.api.Domain.Request.User.CreateUserRequest;
import com.sportshop.api.Domain.Request.User.UpdateUserRequest;
import com.sportshop.api.Domain.Reponse.User.ShippingAddressResponse;
import com.sportshop.api.Domain.Reponse.User.UserResponse;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final ShippingAddressService shippingAddressService;

    public UserController(UserService userService, ShippingAddressService shippingAddressService) {
        this.userService = userService;
        this.shippingAddressService = shippingAddressService;
    }

    /**
     * Tạo user mới (hỗ trợ cả có và không có avatar file)
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateUserRequest request;
        try {
            request = objectMapper.readValue(userJson, CreateUserRequest.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Request body không hợp lệ: " + e.getMessage()));
        }
        UserResponse user = userService.createUser(request, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(user, "Tạo user thành công"));
    }

    /**
     * Lấy danh sách tất cả user
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách user thành công"));
    }

    /**
     * Lấy user theo ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin user thành công"));
    }

    /**
     * Cập nhật thông tin user (hỗ trợ cả có và không có avatar file)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") Long id,
            @RequestPart("user") String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateUserRequest request;
        try {
            request = objectMapper.readValue(userJson, UpdateUserRequest.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Request body không hợp lệ: " + e.getMessage()));
        }
        UserResponse user = userService.updateUser(id, request, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(user, "Cập nhật user thành công"));
    }

    /**
     * Upload avatar cho user
     */
    @PostMapping("/users/{id}/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        UserResponse user = userService.uploadAvatar(id, file);
        return ResponseEntity.ok(ApiResponse.success(user, "Upload avatar thành công"));
    }

    /**
     * Xóa user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa user thành công"));
    }

    /**
     * Tìm user theo email
     */
    @GetMapping("/users/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> findByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user, "Tìm user thành công")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    @GetMapping("/users/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists, "Kiểm tra email thành công"));
    }

    // Xoá avatar
    @DeleteMapping("/users/{id}/avatar")
    public ResponseEntity<ApiResponse<String>> deleteAvatar(@PathVariable Long id) {
        userService.deleteAvatar(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa avatar thành công"));
    }

}
