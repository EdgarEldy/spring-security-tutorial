package edgareldy.springsecuritytutorial.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code POST /api/auth/forgot-password}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public record ForgotPasswordRequest(

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email
) {
}
