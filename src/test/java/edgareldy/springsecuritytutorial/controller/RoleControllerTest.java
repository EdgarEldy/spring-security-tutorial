package edgareldy.springsecuritytutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springsecuritytutorial.config.MethodSecurityConfig;
import edgareldy.springsecuritytutorial.dto.role.RoleRequest;
import edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import edgareldy.springsecuritytutorial.security.CustomPermissionEvaluator;
import edgareldy.springsecuritytutorial.service.RoleService;
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
 * MockMvc integration tests for {@link RoleController}, with
 * {@link RoleService} mocked. {@link MethodSecurityConfig} and
 * {@link CustomPermissionEvaluator} are imported so the class-level
 * {@code @PreAuthorize("hasRole('ADMIN')")} is genuinely enforced.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@WebMvcTest(RoleController.class)
@Import({MethodSecurityConfig.class, CustomPermissionEvaluator.class})
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    private static RoleResponse savedResponse() {
        return new RoleResponse(1L, "ADMIN", List.of());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllReturns200ForAdmin() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(savedResponse()));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleName").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WhenValid() throws Exception {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleService.create(any())).thenReturn(savedResponse());

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleName").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns422WhenAlreadyExists() throws Exception {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleService.create(any())).thenThrow(new BusinessRuleException("Role ADMIN already exists"));

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReturns200WhenValid() throws Exception {
        RoleRequest request = new RoleRequest("MODERATOR");
        when(roleService.update(eq(1L), any())).thenReturn(new RoleResponse(1L, "MODERATOR", List.of()));

        mockMvc.perform(put("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleName").value("MODERATOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReturns404WhenMissing() throws Exception {
        RoleRequest request = new RoleRequest("MODERATOR");
        when(roleService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Role not found with id 99"));

        mockMvc.perform(put("/api/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns200WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Role not found with id 99"))
                .when(roleService).delete(99L);

        mockMvc.perform(delete("/api/roles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignPermissionReturns200ForAdmin() throws Exception {
        when(roleService.assignPermission(1L, 2L)).thenReturn(savedResponse());

        mockMvc.perform(post("/api/roles/1/permissions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void assignPermissionReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/roles/1/permissions/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignPermissionReturns422WhenAlreadyAssigned() throws Exception {
        when(roleService.assignPermission(1L, 2L))
                .thenThrow(new BusinessRuleException("Permission 2 is already assigned to role 1"));

        mockMvc.perform(post("/api/roles/1/permissions/2"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignPermissionReturns404WhenPermissionMissing() throws Exception {
        when(roleService.assignPermission(1L, 99L))
                .thenThrow(new ResourceNotFoundException("Permission not found with id 99"));

        mockMvc.perform(post("/api/roles/1/permissions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removePermissionReturns200ForAdmin() throws Exception {
        when(roleService.removePermission(1L, 2L)).thenReturn(savedResponse());

        mockMvc.perform(delete("/api/roles/1/permissions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
