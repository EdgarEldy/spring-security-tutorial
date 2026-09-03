package com.edgareldy.springsecuritytutorial.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload accepted by {@code POST /api/auth/reset-password}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public record ResetPasswordRequest(

        @NotBlank(message = "token must not be blank")
        String token,

        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 8, message = "newPassword must be at least 8 characters long")
        String newPassword
) {
}
