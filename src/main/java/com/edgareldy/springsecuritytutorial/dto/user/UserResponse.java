package com.edgareldy.springsecuritytutorial.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representation of a {@link com.edgareldy.springsecuritytutorial.entity.User}
 * returned by the API. The {@code password} field is deliberately absent:
 * it must never appear in an HTTP response. {@code roles} lists the
 * assigned role names only, not their permissions; fetch
 * {@code /api/roles} for that detail.
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
        List<String> roles
) {
}
