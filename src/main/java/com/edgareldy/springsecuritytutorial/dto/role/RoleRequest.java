package com.edgareldy.springsecuritytutorial.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code POST}/{@code PUT} {@code /api/roles}. Permission
 * assignment goes through the dedicated
 * {@code /api/roles/{roleId}/permissions/{permissionId}} endpoints, not this
 * request.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record RoleRequest(

        @JsonProperty("role_name")
        @NotBlank(message = "Role name must not be blank")
        String roleName
) {

    public RoleRequest {
        roleName = roleName == null ? null : roleName.trim();
    }
}
