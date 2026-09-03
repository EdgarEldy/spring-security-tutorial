package com.edgareldy.springsecuritytutorial.dto.auth;

/**
 * Response returned by {@code POST /api/auth/login} on success.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public record AuthResponse(
        String token
) {
}
