package com.edgareldy.springsecuritytutorial.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code POST /api/auth/login}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public record LoginRequest(

        @NotBlank(message = "email must not be blank")
        String email,

        @NotBlank(message = "password must not be blank")
        String password
) {
}
