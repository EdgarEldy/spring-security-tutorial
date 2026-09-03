package com.edgareldy.springsecuritytutorial.dto.user;

/**
 * Representation of a {@link com.edgareldy.springsecuritytutorial.entity.User}
 * returned by the API. The {@code password} field is deliberately absent:
 * it must never appear in an HTTP response.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        boolean accountLocked
) {
}
