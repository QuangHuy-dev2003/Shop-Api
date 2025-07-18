package com.sportshop.api.Domain.Reponse.User;

import com.sportshop.api.Domain.Users.Gender;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Gender gender;
    private Long roleId;
    private Boolean firstLogin;
    private LocalDateTime createdAt;
    private String provider;
    private String avatar;
    private Boolean isActive;
    private List<ShippingAddressResponse> shippingAddresses;
}
