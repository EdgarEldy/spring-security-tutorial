package com.edgareldy.springsecuritytutorial.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload used internally by {@code UserService.createUser} to create a
 * disabled account. No controller maps to it directly in feature/users:
 * feature/auth's {@code AuthServiceImpl.register} is the first real caller,
 * building this record from its own {@code RegisterRequest}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record UserRequest(

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
