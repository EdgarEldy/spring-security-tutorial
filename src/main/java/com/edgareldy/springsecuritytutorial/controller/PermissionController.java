package com.edgareldy.springsecuritytutorial.controller;

import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import com.edgareldy.springsecuritytutorial.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD endpoints for permissions. Delegates every
 * operation to {@link PermissionService}; no business logic lives here.
 * Every endpoint is ADMIN-only.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "List permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> findAll() {
        return ResponseEntity.ok(
                ApiResponse.success(permissionService.findAll(), "Permissions retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(permissionService.create(request), "Permission created successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission deleted successfully"));
    }
}
