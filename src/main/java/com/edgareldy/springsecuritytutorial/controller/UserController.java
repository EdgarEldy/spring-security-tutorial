package com.edgareldy.springsecuritytutorial.controller;

import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import com.edgareldy.springsecuritytutorial.dto.common.PageResponse;
import com.edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD and administrative endpoints for users.
 * Delegates every operation to {@link UserService}; no business logic lives
 * here.
 * <p>
 * ADMIN-only endpoints use {@code @PreAuthorize}. The "ADMIN or owner"
 * endpoints ({@link #findById}, {@link #updateProfile}) are checked manually
 * against {@code authentication.getName()} (the user's email) instead of a
 * {@code #id == authentication.principal.id} SpEL expression, because
 * {@code User} only becomes the actual Spring Security principal type once
 * feature/auth wires {@code UserDetailsServiceImpl}; comparing by email
 * keeps this enforceable and testable today and keeps working unchanged
 * once feature/auth exists. The current {@code Authentication} is read from
 * {@link SecurityContextHolder} rather than taken as a method parameter,
 * since the latter is resolved from the request's principal, which is only
 * populated by the security filter chain feature/auth adds later.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users, paginated")
    public ApiResponse<PageResponse<UserResponse>> findAll(Pageable pageable) {
        return ApiResponse.success(userService.findAll(pageable), "Users retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id (admin or the user themselves)")
    public ApiResponse<UserResponse> findById(@PathVariable Long id) {
        assertAdminOrOwner(id);
        return ApiResponse.success(userService.findById(id), "User retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user's profile (admin or the user themselves)")
    public ApiResponse<UserResponse> updateProfile(
            @PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        assertAdminOrOwner(id);
        return ApiResponse.success(userService.updateProfile(id, request), "Profile updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user account")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success(null, "User deleted successfully");
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock a user account")
    public ApiResponse<UserResponse> lock(@PathVariable Long id) {
        Long currentUserId = userService.findByEmail(currentAuthentication().getName()).id();
        return ApiResponse.success(userService.lock(id, currentUserId), "User locked successfully");
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock a user account")
    public ApiResponse<UserResponse> unlock(@PathVariable Long id) {
        return ApiResponse.success(userService.unlock(id), "User unlocked successfully");
    }

    private void assertAdminOrOwner(Long id) {
        Authentication authentication = currentAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
        if (isAdmin) {
            return;
        }
        UserResponse currentUser = userService.findByEmail(authentication.getName());
        if (!currentUser.id().equals(id)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
