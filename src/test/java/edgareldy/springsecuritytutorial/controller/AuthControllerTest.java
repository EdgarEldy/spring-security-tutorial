package edgareldy.springsecuritytutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springsecuritytutorial.dto.auth.AuthResponse;
import edgareldy.springsecuritytutorial.dto.auth.ForgotPasswordRequest;
import edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import edgareldy.springsecuritytutorial.dto.auth.ResetPasswordRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import edgareldy.springsecuritytutorial.security.JwtService;
import edgareldy.springsecuritytutorial.security.UserDetailsServiceImpl;
import edgareldy.springsecuritytutorial.service.AuthService;
import edgareldy.springsecuritytutorial.service.BlacklistedTokenService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link AuthController}, with
 * {@link AuthService} mocked. Servlet filters are disabled, so
 * {@code /me} and {@code /logout} rely on {@code @WithMockUser} rather
 * than a real JWT going through {@code JwtAuthFilter}. {@code JwtAuthFilter}
 * itself still needs to be instantiated though, since {@code @WebMvcTest}
 * auto-includes any {@code Filter} bean regardless of {@code addFilters};
 * its own collaborators ({@link JwtService}, {@link BlacklistedTokenService},
 * {@link UserDetailsServiceImpl}) are mocked purely so the context loads.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private BlacklistedTokenService blacklistedTokenService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_returnsCreatedUser() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "jane@example.com", "password1");
        UserResponse response = new UserResponse(1L, "Jane", "Doe", "jane@example.com", false, false, List.of());
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));
    }

    @Test
    void register_rejectsInvalidPayload() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "not-an-email", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void activateAccount_delegatesToService() throws Exception {
        mockMvc.perform(get("/api/auth/activate-account").param("token", "activation-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).activateAccount("activation-token");
    }

    @Test
    void login_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "password1");
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void logout_extractsBearerTokenAndDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout("jwt-token");
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void me_returnsCurrentUser() throws Exception {
        UserResponse response = new UserResponse(1L, "Jane", "Doe", "jane@example.com", true, false, List.of());
        when(authService.me()).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));
    }

    @Test
    void forgotPassword_delegatesToService() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("jane@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).forgotPassword(eq("jane@example.com"));
    }

    @Test
    void resetPassword_delegatesToService() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).resetPassword("reset-token", "newPassword1");
    }
}
