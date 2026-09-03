package com.edgareldy.springsecuritytutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.edgareldy.springsecuritytutorial.config.MethodSecurityConfig;
import com.edgareldy.springsecuritytutorial.security.CustomPermissionEvaluator;
import com.edgareldy.springsecuritytutorial.dto.common.PageResponse;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import com.edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import com.edgareldy.springsecuritytutorial.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link UserController}, with
 * {@link UserService} mocked. Servlet filters are disabled (no CSRF/default
 * auth noise), but {@link MethodSecurityConfig} is imported so
 * {@code @PreAuthorize} is genuinely enforced against the
 * {@code @WithMockUser} principal in each test.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@WebMvcTest(UserController.class)
@Import({MethodSecurityConfig.class, CustomPermissionEvaluator.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static UserResponse owner() {
        return new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of());
    }

    private static UserResponse someoneElse() {
        return new UserResponse(2L, "Eve", "Stranger", "eve@example.com", true, false, List.of());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllReturns200ForAdmin() throws Exception {
        PageResponse<UserResponse> page = new PageResponse<>(List.of(owner()), 0, 20, 1, 1);
        when(userService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("ada@example.com"))
                .andExpect(jsonPath("$.data.total_elements").value(1))
                .andExpect(jsonPath("$.data.total_pages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ada@example.com")
    void findByIdReturns200ForOwner() throws Exception {
        when(userService.findByEmail("ada@example.com")).thenReturn(owner());
        when(userService.findById(1L)).thenReturn(owner());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("ada@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByIdReturns200WithNestedRolesAndPermissions() throws Exception {
        PermissionResponse readUsers = new PermissionResponse(1L, "USER", "READ");
        RoleResponse adminRole = new RoleResponse(1L, "ADMIN", List.of(readUsers));
        UserResponse withRole =
                new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of(adminRole));
        when(userService.findById(1L)).thenReturn(withRole);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0].role_name").value("ADMIN"))
                .andExpect(jsonPath("$.data.roles[0].permissions[0].resource").value("USER"))
                .andExpect(jsonPath("$.data.roles[0].permissions[0].action").value("READ"));
    }

    @Test
    @WithMockUser(username = "eve@example.com")
    void findByIdReturns403ForNonOwnerNonAdmin() throws Exception {
        when(userService.findByEmail("eve@example.com")).thenReturn(someoneElse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());

        verify(userService, never()).findById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByIdReturns200ForAdminWithoutOwnerLookup() throws Exception {
        when(userService.findById(1L)).thenReturn(owner());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());

        verify(userService, never()).findByEmail(any());
    }

    @Test
    @WithMockUser(username = "ada@example.com")
    void updateProfileReturns200ForOwner() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Ada", "Byron");
        UserResponse updated = new UserResponse(1L, "Ada", "Byron", "ada@example.com", true, false, List.of());
        when(userService.findByEmail("ada@example.com")).thenReturn(owner());
        when(userService.updateProfile(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.last_name").value("Byron"));
    }

    @Test
    @WithMockUser(username = "eve@example.com")
    void updateProfileReturns403ForNonOwnerNonAdmin() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Ada", "Byron");
        when(userService.findByEmail("eve@example.com")).thenReturn(someoneElse());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    @WithMockUser(username = "ada@example.com")
    void updateProfileReturns400WhenValidationFails() throws Exception {
        when(userService.findByEmail("ada@example.com")).thenReturn(owner());
        UpdateProfileRequest blank = new UpdateProfileRequest(" ", "");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blank)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProfileReturns404WhenMissing() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Ada", "Byron");
        when(userService.updateProfile(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("User not found with id 99"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns200ForAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void lockReturns200ForAdmin() throws Exception {
        UserResponse admin = new UserResponse(2L, "Admin", "Person", "admin@example.com", true, false, List.of());
        UserResponse locked = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, true, List.of());
        when(userService.findByEmail("admin@example.com")).thenReturn(admin);
        when(userService.lock(1L, 2L)).thenReturn(locked);

        mockMvc.perform(patch("/api/users/1/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account_locked").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void lockReturns422WhenAdminLocksThemselves() throws Exception {
        UserResponse admin = new UserResponse(1L, "Admin", "Person", "admin@example.com", true, false, List.of());
        when(userService.findByEmail("admin@example.com")).thenReturn(admin);
        when(userService.lock(1L, 1L)).thenThrow(new BusinessRuleException("An admin cannot lock their own account"));

        mockMvc.perform(patch("/api/users/1/lock"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "USER")
    void lockReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(patch("/api/users/1/lock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlockReturns200ForAdmin() throws Exception {
        UserResponse unlocked = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of());
        when(userService.unlock(1L)).thenReturn(unlocked);

        mockMvc.perform(patch("/api/users/1/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account_locked").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void unlockReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(patch("/api/users/1/unlock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleReturns200ForAdmin() throws Exception {
        RoleResponse adminRole = new RoleResponse(2L, "ADMIN", List.of());
        UserResponse withRole =
                new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of(adminRole));
        when(userService.assignRole(1L, 2L)).thenReturn(withRole);

        mockMvc.perform(post("/api/users/1/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0].role_name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void assignRoleReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/users/1/roles/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleReturns422WhenAlreadyAssigned() throws Exception {
        when(userService.assignRole(1L, 2L))
                .thenThrow(new BusinessRuleException("Role 2 is already assigned to user 1"));

        mockMvc.perform(post("/api/users/1/roles/2"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRoleReturns200ForAdmin() throws Exception {
        when(userService.removeRole(1L, 2L)).thenReturn(owner());

        mockMvc.perform(delete("/api/users/1/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeRoleReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/roles/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRoleReturns422WhenNotAssigned() throws Exception {
        when(userService.removeRole(1L, 2L))
                .thenThrow(new BusinessRuleException("Role 2 is not assigned to user 1"));

        mockMvc.perform(delete("/api/users/1/roles/2"))
                .andExpect(status().isUnprocessableEntity());
    }
}
