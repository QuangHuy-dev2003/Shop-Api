package com.sportshop.api.Domain.Request.ShippingAddress;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressRequest {

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String addressLine;

    @Size(max = 100, message = "Phường/xã không được vượt quá 100 ký tự")
    private String ward;

    @Size(max = 100, message = "Quận/huyện không được vượt quá 100 ký tự")
    private String district;

    @Size(max = 100, message = "Tỉnh/thành phố không được vượt quá 100 ký tự")
    private String province;

    private Boolean isDefault = false;
}