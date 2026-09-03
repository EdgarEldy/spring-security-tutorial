package com.edgareldy.springsecuritytutorial.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code PUT /api/users/{id}}. Only the fields an
 * account owner (or an admin) may self-service are exposed here; email,
 * password, and account state changes go through dedicated endpoints.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record UpdateProfileRequest(

        @JsonProperty("first_name")
        @NotBlank(message = "First name must not be blank")
        String firstName,

        @JsonProperty("last_name")
        @NotBlank(message = "Last name must not be blank")
        String lastName
) {
}
