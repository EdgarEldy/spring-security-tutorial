package edgareldy.springsecuritytutorial.controller;

import edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import edgareldy.springsecuritytutorial.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public ApiResponse<List<PermissionResponse>> findAll() {
        return ApiResponse.success(permissionService.findAll(), "Permissions retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a permission")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ApiResponse.success(permissionService.create(request), "Permission created successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.success(null, "Permission deleted successfully");
    }
}
