package com.sportshop.api.Config;

import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Role;
import com.sportshop.api.Domain.Permissions;
import com.sportshop.api.Service.UserService;
import com.sportshop.api.Service.RoleService;
import com.sportshop.api.Service.PermissionService;
import com.sportshop.api.Config.JwtUtil;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportshop.api.Domain.Reponse.ApiResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    private final RoleService roleService;

    private final PermissionService permissionService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public PermissionInterceptor(JwtUtil jwtUtil, UserService userService, RoleService roleService,
            PermissionService permissionService, UserRepository userRepository, RoleRepository roleRepository) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Chỉ kiểm tra cho các method handler
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Danh sách các API public (không cần xác thực)
        if (isPublicAPI(requestURI, method)) {
            return true;
        }

        // Lấy token từ header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String errorResponse = createErrorResponse(false, "Token không hợp lệ",
                    "Vui lòng đăng nhập để lấy token", null);
            response.getWriter().write(errorResponse);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            // Xác thực token
            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                String errorResponse = createErrorResponse(false, "Token không hợp lệ",
                        "Token không chứa thông tin email hợp lệ", null);
                response.getWriter().write(errorResponse);
                return false;
            }

            // Lấy thông tin user từ repository
            Optional<Users> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                String errorResponse = createErrorResponse(false, "User không tồn tại",
                        "Không tìm thấy user với email: " + email, null);
                response.getWriter().write(errorResponse);
                return false;
            }

            Users user = userOpt.get();

            // Kiểm tra role_id từ token có khớp với database không
            Long tokenRoleId = jwtUtil.extractRoleId(token);
            if (tokenRoleId != null && !tokenRoleId.equals(user.getRoleId())) {
                System.out
                        .println("==> Role mismatch: Token roleId=" + tokenRoleId + ", DB roleId=" + user.getRoleId());
                // Cập nhật role_id trong database từ token
                user.setRoleId(tokenRoleId);
                userRepository.save(user);
                System.out.println("==> Updated user roleId to match token");
            }

            System.out.println("==> Email: " + user.getEmail() + ", roleId: " + user.getRoleId());

            // Kiểm tra quyền truy cập
            if (!hasPermission(user, requestURI, method)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");

                // Tạo thông tin chi tiết về quyền
                String requiredPermission = getRequiredPermission(requestURI, method);
                String userRole = getUserRoleName(user);
                String details = String.format("User role: %s, Required permission: %s, API: %s %s",
                        userRole, requiredPermission != null ? requiredPermission : "N/A", method, requestURI);

                String errorResponse = createErrorResponse(false, "Không có quyền truy cập",
                        details, createPermissionErrorData(user, requestURI, method, requiredPermission));
                response.getWriter().write(errorResponse);
                return false;
            }

            // Lưu user vào request attribute để sử dụng trong controller
            request.setAttribute("currentUser", user);
            return true;

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String errorResponse = createErrorResponse(false, "Token không hợp lệ",
                    "Token đã hết hạn hoặc không hợp lệ", null);
            response.getWriter().write(errorResponse);
            return false;
        }
    }

    /**
     * Tạo JSON response theo format ApiResponse
     */
    private String createErrorResponse(boolean success, String message, String details, Object data) {
        try {
            ApiResponse<Object> apiResponse = new ApiResponse<>(success, message, data, LocalDateTime.now());
            return objectMapper.writeValueAsString(apiResponse);
        } catch (Exception e) {
            // Fallback nếu có lỗi serialize
            return String.format(
                    "{\"success\":%s,\"message\":\"%s\",\"details\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                    success, message, details, LocalDateTime.now());
        }
    }

    /**
     * Lấy tên role của user
     */
    private String getUserRoleName(Users user) {
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return "NO_ROLE";
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return "ROLE_NOT_FOUND";
        }

        return roleOpt.get().getName();
    }

    /**
     * Tạo data chi tiết cho lỗi permission
     */
    private Object createPermissionErrorData(Users user, String requestURI, String method, String requiredPermission) {
        return Map.of(
                "userId", user.getId(),
                "userEmail", user.getEmail(),
                "userRole", getUserRoleName(user),
                "requestedApi", method + " " + requestURI,
                "requiredPermission", requiredPermission != null ? requiredPermission : "N/A",
                "suggestion", "Liên hệ admin để được cấp quyền truy cập");
    }

    /**
     * Kiểm tra API có phải là public không
     */
    private boolean isPublicAPI(String requestURI, String method) {
        // Auth APIs - luôn public
        if (requestURI.startsWith("/api/v1/auth/"))
            return true;
        // OTP APIs - luôn public
        if (requestURI.startsWith("/api/v1/otp/"))
            return true;
        // Email OTP APIs
        if (requestURI.startsWith("/api/v1/email-otp/"))
            return true;
        // VNPay APIs - luôn public
        if (requestURI.startsWith("/api/v1/vnpay/"))
            return true;

        // Sản phẩm - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/products"))
            return true;
        if (method.equals("GET") && requestURI.equals("/api/v1/download-excel-template"))
            return true;

        // Danh mục - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/categories"))
            return true;

        // Thương hiệu - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/brands"))
            return true;

        // Mã giảm giá - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/discounts"))
            return true;
        if (requestURI.equals("/api/v1/discounts/validate") && method.equals("POST"))
            return true;

        // Cart - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/cart/"))
            return true;

        // Favorites - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/favorites/"))
            return true;

        // Order - public cho các API user thao tác
        if (requestURI.equals("/api/v1/orders/place-order") && method.equals("POST"))
            return true;
        if (requestURI.matches("/api/v1/orders/user/\\d+") && method.equals("GET"))
            return true;
        if (requestURI.matches("/api/v1/orders/\\d+/cancel/user") && method.equals("GET"))
            return true;
        if (requestURI.matches("/api/v1/orders/\\d+") && method.equals("GET"))
            return true;

        // Shipping address - public GET
        if (method.equals("GET") && requestURI.startsWith("/api/v1/shipping-addresses/"))
            return true;

        // Lấy user theo email - public GET
        if (method.equals("GET") && requestURI.matches("/api/v1/users/email/.+"))
            return true;

        // Avatar upload/delete - public
        if (requestURI.matches("/api/v1/users/\\d+/avatar") && (method.equals("POST") || method.equals("DELETE")))
            return true;

        // User update - public
        if (requestURI.matches("/api/v1/users/\\d+") && method.equals("PUT"))
            return true;

        // User change password - public
        if (requestURI.matches("/api/v1/auth/change-password") && method.equals("POST"))
            return true;

        // Product Search - public
        if (requestURI.matches("/api/v1/products/search") && method.equals("GET"))
            return true;

        // Các API public khác nếu có thể bổ sung ở đây

        return false;
    }

    /**
     * Kiểm tra user có quyền truy cập API không
     */
    private boolean hasPermission(Users user, String requestURI, String method) {
        // SUPER_ADMIN có tất cả quyền
        if (isSuperAdmin(user)) {
            return true;
        }

        // Lấy role của user
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return false;
        }

        Role userRole = roleOpt.get();

        // Lấy permissions của role
        List<Permissions> permissions = userRole.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // Kiểm tra permission dựa trên URI và method
        String requiredPermission = getRequiredPermission(requestURI, method);
        if (requiredPermission == null) {
            return true; // Không cần permission cụ thể
        }

        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(requiredPermission));
    }

    /**
     * Kiểm tra user có phải SUPER_ADMIN không
     */
    private boolean isSuperAdmin(Users user) {
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return false;
        }

        // SUPER_ADMIN có roleId = 1
        return roleId == 1L;
    }

    /**
     * Xác định permission cần thiết dựa trên URI và method
     */
    private String getRequiredPermission(String requestURI, String method) {
        // SUPER_ADMIN có tất cả quyền - không cần kiểm tra permission cụ thể
        // Chỉ kiểm tra cho USER role

        // User management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/users")) {
            if (method.equals("GET")) {
                return "USER_READ";
            } else if (method.equals("POST")) {
                return "USER_CREATE";
            } else if (method.equals("PUT")) {
                return "USER_UPDATE";
            } else if (method.equals("DELETE")) {
                return "USER_DELETE";
            }
        }

        // Product management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/products")) {
            if (method.equals("POST") || requestURI.contains("/import-products-from-excel")) {
                return "PRODUCT_CREATE";
            } else if (method.equals("PUT")) {
                return "PRODUCT_UPDATE";
            } else if (method.equals("DELETE")) {
                return "PRODUCT_DELETE";
            } else if (requestURI.contains("/upload-images")) {
                return "PRODUCT_UPLOAD";
            }
        }

        // Category management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/categories")) {
            if (method.equals("POST")) {
                return "CATEGORY_CREATE";
            } else if (method.equals("PUT")) {
                return "CATEGORY_UPDATE";
            } else if (method.equals("DELETE")) {
                return "CATEGORY_DELETE";
            }
        }

        // Brand management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/brands")) {
            if (method.equals("POST")) {
                return "BRAND_CREATE";
            } else if (method.equals("PUT")) {
                return "BRAND_UPDATE";
            } else if (method.equals("DELETE")) {
                return "BRAND_DELETE";
            }
        }

        // Order management
        if (requestURI.startsWith("/api/v1/orders")) {
            if (method.equals("GET") && requestURI.equals("/api/v1/orders")) {
                return "ORDER_READ_ALL"; // Chỉ SUPER_ADMIN
            } else if (method.equals("PUT") || method.equals("DELETE")) {
                return "ORDER_MANAGE"; // Chỉ SUPER_ADMIN
            } else if (requestURI.contains("/cancel/admin")) {
                return "ORDER_CANCEL_ADMIN"; // Chỉ SUPER_ADMIN
            }
            // USER có thể đặt hàng và xem đơn hàng của mình
        }

        // Discount management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/discounts")) {
            if (method.equals("POST")) {
                return "DISCOUNT_CREATE";
            } else if (method.equals("PUT")) {
                return "DISCOUNT_UPDATE";
            } else if (method.equals("DELETE")) {
                return "DISCOUNT_DELETE";
            } else if (method.equals("PATCH")) {
                return "DISCOUNT_TOGGLE";
            }
        }

        // Role management - chỉ SUPER_ADMIN
        if (requestURI.startsWith("/api/v1/roles")) {
            return "ROLE_MANAGE";
        }

        // Cart operations - USER có thể truy cập giỏ hàng của mình
        if (requestURI.startsWith("/api/v1/cart/")) {
            return "CART_ACCESS";
        }

        return null; // Không cần permission cụ thể
    }
}