package com.edgareldy.springsecuritytutorial.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload accepted by {@code POST /api/auth/register}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public record RegisterRequest(

        @NotBlank(message = "firstName must not be blank")
        String firstName,

        @NotBlank(message = "lastName must not be blank")
        String lastName,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "password must be at least 8 characters long")
        String password
) {
}
