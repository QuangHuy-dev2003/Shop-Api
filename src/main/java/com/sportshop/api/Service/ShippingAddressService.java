package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.sportshop.api.Repository.ShippingAddressRepository;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Domain.Shipping_addresses;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.Request.ShippingAddress.CreateAddressRequest;
import com.sportshop.api.Domain.Request.ShippingAddress.UpdateAddressRequest;
import com.sportshop.api.Domain.Reponse.User.ShippingAddressResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShippingAddressService(ShippingAddressRepository shippingAddressRepository,
            UserRepository userRepository) {
        this.shippingAddressRepository = shippingAddressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy danh sách địa chỉ của user
     */
    public List<ShippingAddressResponse> getUserAddresses(Long userId) {
        List<Shipping_addresses> addresses = shippingAddressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToAddressResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo địa chỉ mới
     */
    @Transactional
    public ShippingAddressResponse createAddress(Long userId, CreateAddressRequest request) {
        // Kiểm tra user có tồn tại không
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Nếu địa chỉ mới là mặc định, reset tất cả địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            shippingAddressRepository.resetDefaultAddressByUserId(userId);
        }

        // Tạo địa chỉ mới
        Shipping_addresses address = new Shipping_addresses();
        address.setUser(user);
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setProvince(request.getProvince());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        Shipping_addresses savedAddress = shippingAddressRepository.save(address);
        return convertToAddressResponse(savedAddress);
    }

    /**
     * Cập nhật địa chỉ
     */
    @Transactional
    public ShippingAddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        // Kiểm tra địa chỉ có thuộc về user không
        Shipping_addresses address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không thuộc về user này");
        }

        // Nếu địa chỉ được đặt làm mặc định, reset tất cả địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            shippingAddressRepository.resetDefaultAddressByUserId(userId);
        }

        // Cập nhật thông tin địa chỉ
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
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        Shipping_addresses updatedAddress = shippingAddressRepository.save(address);
        return convertToAddressResponse(updatedAddress);
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @Transactional
    public ShippingAddressResponse setDefaultAddress(Long userId, Long addressId) {
        // Kiểm tra địa chỉ có thuộc về user không
        Shipping_addresses address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không thuộc về user này");
        }

        // Reset tất cả địa chỉ về không mặc định
        shippingAddressRepository.resetDefaultAddressByUserId(userId);

        // Đặt địa chỉ này làm mặc định
        address.setIsDefault(true);
        Shipping_addresses updatedAddress = shippingAddressRepository.save(address);
        return convertToAddressResponse(updatedAddress);
    }

    /**
     * Xóa địa chỉ
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // Kiểm tra địa chỉ có thuộc về user không
        Shipping_addresses address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không thuộc về user này");
        }

        // Nếu địa chỉ này là mặc định và còn địa chỉ khác, đặt địa chỉ khác làm mặc
        // định
        if (address.getIsDefault()) {
            List<Shipping_addresses> otherAddresses = shippingAddressRepository.findByUserId(userId);
            otherAddresses.removeIf(addr -> addr.getId().equals(addressId));

            if (!otherAddresses.isEmpty()) {
                Shipping_addresses newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(true);
                shippingAddressRepository.save(newDefault);
            }
        }

        shippingAddressRepository.delete(address);
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
}