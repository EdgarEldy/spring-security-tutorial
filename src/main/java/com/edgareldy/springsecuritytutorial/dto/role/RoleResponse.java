package com.edgareldy.springsecuritytutorial.dto.role;

import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Representation of a {@link com.edgareldy.springsecuritytutorial.entity.Role}
 * returned by the API, including its currently assigned permissions.
 * {@code permissions} is populated on every endpoint that returns a
 * {@code RoleResponse}, including the (unpaginated) {@code GET /api/roles}
 * list: unlike users and their roles, roles and permissions are reference
 * data with a small, effectively fixed cardinality, so the extra fetch per
 * role is not worth special-casing away.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record RoleResponse(
        Long id,
        @JsonProperty("role_name") String roleName,
        List<PermissionResponse> permissions
) {
}
