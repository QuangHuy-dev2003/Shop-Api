package com.sportshop.api.Domain.Reponse.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String apiPath;
    private String method;
    private String module;
    private Boolean active;
}