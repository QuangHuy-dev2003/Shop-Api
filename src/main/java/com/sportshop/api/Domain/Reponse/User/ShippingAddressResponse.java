package com.sportshop.api.Domain.Reponse.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressResponse {
    private Long id;
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private Boolean isDefault;
}