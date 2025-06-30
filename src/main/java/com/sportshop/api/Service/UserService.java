package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Repository.ShippingAddressRepository;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Shipping_addresses;
import com.sportshop.api.Domain.Request.User.CreateUserRequest;
import com.sportshop.api.Domain.Request.User.UpdateUserRequest;
import com.sportshop.api.Domain.Reponse.User.ShippingAddressResponse;
import com.sportshop.api.Domain.Reponse.User.UserResponse;
import com.sportshop.api.Repository.RoleRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository,
            ShippingAddressRepository shippingAddressRepository,
            CloudinaryService cloudinaryService,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    /**
     * Tạo user mới (hỗ trợ cả có và không có avatar)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, MultipartFile avatarFile) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        // Kiểm tra phone nếu có
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
            }
        }
        // Kiểm tra roleId có tồn tại không
        if (request.getRoleId() != null) {
            if (!roleRepository.existsById(request.getRoleId())) {
                throw new RuntimeException("Role không tồn tại");
            }
        }

        // Tạo user mới
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRoleId(request.getRoleId());
        user.setFirstLogin(true);
        user.setProvider(Users.Provider.DEFAULT);
        user.setGender(Users.Gender.valueOf(request.getGender()));

        // Xử lý avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Upload avatar lên Cloudinary
                String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload avatar: " + e.getMessage());
            }
        }

        // Lưu user
        Users savedUser = userRepository.save(user);

        // Tạo địa chỉ mặc định nếu có thông tin địa chỉ
        if (hasAddressInfo(request)) {
            createDefaultShippingAddress(savedUser, request);
        }

        return convertToUserResponse(savedUser);
    }

    /**
     * Tạo user mới (không có avatar file)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        return createUser(request, null);
    }

    /**
     * Lấy danh sách tất cả user
     */
    public List<UserResponse> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy user theo ID
     */
    public UserResponse getUserById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        return convertToUserResponse(user);
    }

    /**
     * Cập nhật thông tin user
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, MultipartFile avatarFile) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));

        // Kiểm tra email nếu có thay đổi
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống");
            }
            user.setEmail(request.getEmail());
        }

        // Kiểm tra phone nếu có thay đổi
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
            }
            user.setPhone(request.getPhone());
        }

        // Cập nhật các trường khác
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getRoleId() != null) {
            user.setRoleId(request.getRoleId());
        }
        if (request.getFirstLogin() != null) {
            user.setFirstLogin(request.getFirstLogin());
        }
        if (request.getGender() != null) {
            user.setGender(Users.Gender.valueOf(request.getGender()));
        }

        // Xử lý avatar - ưu tiên file upload trước, sau đó mới đến URL
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Có file upload - xóa avatar cũ và upload mới
            try {
                // Xóa avatar cũ trên Cloudinary nếu có
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
                    if (publicId != null) {
                        cloudinaryService.deleteImage(publicId);
                    }
                }

                // Upload avatar mới lên Cloudinary
                String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                user.setAvatar(avatarUrl);

            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload avatar: " + e.getMessage());
            }
        } else if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            // Có URL avatar - xóa avatar cũ và cập nhật URL mới
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            }
            user.setAvatar(request.getAvatar());
        }
        // Nếu không có avatar mới thì giữ nguyên avatar cũ

        // Cập nhật địa chỉ nếu có thông tin địa chỉ
        if (hasAddressInfo(request)) {
            updateOrCreateShippingAddress(user, request);
        }

        Users updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    /**
     * Cập nhật thông tin user (không có avatar file)
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        return updateUser(id, request, null);
    }

    /**
     * Upload avatar cho user
     */
    @Transactional
    public UserResponse uploadAvatar(Long userId, MultipartFile file) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        try {
            // Xóa avatar cũ nếu có
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            }

            // Upload avatar mới
            String avatarUrl = cloudinaryService.uploadImage(file, "avatars");
            user.setAvatar(avatarUrl);

            Users savedUser = userRepository.save(user);
            return convertToUserResponse(savedUser);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload avatar: " + e.getMessage());
        }
    }

    /**
     * Xóa avatar của user
     */
    @Transactional
    public UserResponse deleteAvatar(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            // Xóa ảnh trên Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }

            // Xóa URL avatar
            user.setAvatar(null);
            Users savedUser = userRepository.save(user);
            return convertToUserResponse(savedUser);
        }

        return convertToUserResponse(user);
    }

    /**
     * Xóa user
     */
    @Transactional
    public void deleteUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));

        // Xóa avatar trên Cloudinary nếu có
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        }

        // Xóa tất cả địa chỉ của user
        shippingAddressRepository.deleteByUserId(id);

        // Xóa user
        userRepository.delete(user);
    }

    /**
     * Tìm user theo email
     */
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToUserResponse);
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Chuyển đổi Users thành UserResponse
     */
    private UserResponse convertToUserResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRoleId(user.getRoleId());
        response.setFirstLogin(user.getFirstLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setProvider(user.getProvider().name());
        response.setAvatar(user.getAvatar());

        // Lấy danh sách địa chỉ
        List<Shipping_addresses> addresses = shippingAddressRepository.findByUserId(user.getId());
        List<ShippingAddressResponse> addressResponses = addresses.stream()
                .map(this::convertToAddressResponse)
                .collect(Collectors.toList());
        response.setShippingAddresses(addressResponses);

        return response;
    }

    /**
     * Chuyển đổi Shipping_addresses thành ShippingAddressResponse
     */
    private ShippingAddressResponse convertToAddressResponse(Shipping_addresses address) {
        ShippingAddressResponse response = new ShippingAddressResponse();
        response.setId(address.getId());
        response.setAddressLine(address.getAddressLine());
        response.setWard(address.getWard());
        response.setDistrict(address.getDistrict());
        response.setProvince(address.getProvince());
        response.setIsDefault(address.getIsDefault());
        return response;
    }

    /**
     * Cập nhật hoặc tạo địa chỉ cho user
     */
    private void updateOrCreateShippingAddress(Users user, UpdateUserRequest request) {
        // Kiểm tra user đã có địa chỉ mặc định chưa
        var existingDefaultAddress = shippingAddressRepository.findByUserIdAndIsDefaultTrue(user.getId());

        if (existingDefaultAddress.isPresent()) {
            // Cập nhật địa chỉ mặc định hiện tại
            Shipping_addresses address = existingDefaultAddress.get();
            if (request.getAddressLine() != null) {
                address.setAddressLine(request.getAddressLine());
            }
            if (request.getWard() != null) {
                address.setWard(request.getWard());
            }
            if (request.getDistrict() != null) {
                address.setDistrict(request.getDistrict());
            }
            if (request.getProvince() != null) {
                address.setProvince(request.getProvince());
            }
            shippingAddressRepository.save(address);
        } else {
            // Tạo địa chỉ mới nếu chưa có
            Shipping_addresses address = new Shipping_addresses();
            address.setUser(user);
            address.setAddressLine(request.getAddressLine());
            address.setWard(request.getWard());
            address.setDistrict(request.getDistrict());
            address.setProvince(request.getProvince());
            address.setIsDefault(true);
            shippingAddressRepository.save(address);
        }
    }

    /**
     * Kiểm tra có thông tin địa chỉ không
     */
    private boolean hasAddressInfo(CreateUserRequest request) {
        return (request.getAddressLine() != null && !request.getAddressLine().trim().isEmpty()) ||
                (request.getWard() != null && !request.getWard().trim().isEmpty()) ||
                (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) ||
                (request.getProvince() != null && !request.getProvince().trim().isEmpty());
    }

    /**
     * Kiểm tra có thông tin địa chỉ không (cho UpdateUserRequest)
     */
    private boolean hasAddressInfo(UpdateUserRequest request) {
        return (request.getAddressLine() != null && !request.getAddressLine().trim().isEmpty()) ||
                (request.getWard() != null && !request.getWard().trim().isEmpty()) ||
                (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) ||
                (request.getProvince() != null && !request.getProvince().trim().isEmpty());
    }

    /**
     * Tạo địa chỉ mặc định cho user
     */
    private void createDefaultShippingAddress(Users user, CreateUserRequest request) {
        Shipping_addresses address = new Shipping_addresses();
        address.setUser(user);
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setProvince(request.getProvince());
        address.setIsDefault(true);

        shippingAddressRepository.save(address);
    }
}
