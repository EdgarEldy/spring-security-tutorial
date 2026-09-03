package com.edgareldy.springsecuritytutorial.dto.user;

import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Representation of a {@link com.edgareldy.springsecuritytutorial.entity.User}
 * returned by the API. The {@code password} field is deliberately absent:
 * it must never appear in an HTTP response. {@code roles} (with their
 * nested permissions) is only populated where the caller specifically asked
 * about a user's roles: the single-user detail endpoint
 * ({@code GET /api/users/{id}}) and the role assignment endpoints
 * ({@code POST}/{@code DELETE /api/users/{id}/roles/{roleId}}). Every other
 * endpoint that returns a {@code UserResponse} (the paginated list, profile
 * update, lock/unlock) leaves it as an empty list, so those never pay the
 * extra role/permission fetch per user.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record UserResponse(
        Long id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String email,
        boolean enabled,
        @JsonProperty("account_locked") boolean accountLocked,
        List<RoleResponse> roles
) {
}
