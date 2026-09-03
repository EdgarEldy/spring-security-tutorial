package com.edgareldy.springsecuritytutorial.controller;

import com.edgareldy.springsecuritytutorial.dto.auth.AuthResponse;
import com.edgareldy.springsecuritytutorial.dto.auth.ForgotPasswordRequest;
import com.edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import com.edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import com.edgareldy.springsecuritytutorial.dto.auth.ResetPasswordRequest;
import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the authentication flow: registration, account
 * activation, login, logout, current-user profile, and password reset.
 * Delegates every operation to {@link AuthService}; no business logic lives
 * here.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new account")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request), "Account registered successfully");
    }

    @GetMapping("/activate-account")
    @SecurityRequirements
    @Operation(summary = "Activate an account using the token emailed at registration")
    public ApiResponse<Void> activateAccount(@RequestParam String token) {
        authService.activateAccount(token);
        return ApiResponse.success(null, "Account activated successfully");
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate and obtain a JWT")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request), "Login successful");
    }

    @PostMapping("/logout")
    @Operation(summary = "Blacklist the current JWT")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(extractToken(request));
        return ApiResponse.success(null, "Logout successful");
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(authService.me(), "Current user retrieved successfully");
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements
    @Operation(summary = "Request a password reset email")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ApiResponse.success(null, "Password reset email sent");
    }

    @PostMapping("/reset-password")
    @SecurityRequirements
    @Operation(summary = "Reset the password using the token emailed on request")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.success(null, "Password reset successfully");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header.substring(BEARER_PREFIX.length());
    }
}
