package edgareldy.springsecuritytutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springsecuritytutorial.config.MethodSecurityConfig;
import edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import edgareldy.springsecuritytutorial.security.CustomPermissionEvaluator;
import edgareldy.springsecuritytutorial.service.PermissionService;
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
 * MockMvc integration tests for {@link PermissionController}, with
 * {@link PermissionService} mocked. {@link MethodSecurityConfig} and
 * {@link CustomPermissionEvaluator} are imported so the class-level
 * {@code @PreAuthorize("hasRole('ADMIN')")} is genuinely enforced.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@WebMvcTest(PermissionController.class)
@Import({MethodSecurityConfig.class, CustomPermissionEvaluator.class})
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    private static PermissionResponse savedResponse() {
        return new PermissionResponse(1L, "PRODUCT", "WRITE");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllReturns200ForAdmin() throws Exception {
        when(permissionService.findAll()).thenReturn(List.of(savedResponse()));

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].resource").value("PRODUCT"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WhenValid() throws Exception {
        PermissionRequest request = new PermissionRequest("PRODUCT", "WRITE");
        when(permissionService.create(any())).thenReturn(savedResponse());

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.action").value("WRITE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns400WhenBlank() throws Exception {
        PermissionRequest invalid = new PermissionRequest(" ", "");

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns422WhenAlreadyExists() throws Exception {
        PermissionRequest request = new PermissionRequest("PRODUCT", "WRITE");
        when(permissionService.create(any()))
                .thenThrow(new BusinessRuleException("Permission PRODUCT:WRITE already exists"));

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns200WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isForbidden());

        verify(permissionService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns422WhenAssignedToRole() throws Exception {
        doThrow(new BusinessRuleException("Permission 1 is assigned to at least one role and cannot be deleted"))
                .when(permissionService).delete(eq(1L));

        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isUnprocessableEntity());
    }
}
