package edgareldy.springsecuritytutorial;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springsecuritytutorial.dto.auth.ForgotPasswordRequest;
import edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import edgareldy.springsecuritytutorial.dto.auth.ResetPasswordRequest;
import edgareldy.springsecuritytutorial.entity.ActivationToken;
import edgareldy.springsecuritytutorial.entity.PasswordResetToken;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import edgareldy.springsecuritytutorial.repository.PasswordResetTokenRepository;
import edgareldy.springsecuritytutorial.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end integration test for the full authentication flow, exercised
 * through real HTTP calls against the actual {@code SecurityFilterChain}
 * (filters are not disabled here, unlike the {@code @WebMvcTest} slices),
 * a real Postgres instance via Testcontainers, and real JWTs. Covers both
 * the happy path (register, activate, login, access a protected endpoint,
 * logout, then rejection) and the error cases the README calls out
 * explicitly: unactivated account, locked account, expired/already-used
 * token, and wrong credentials.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void fullFlow_registerActivateLoginAccessProtectedLogoutThenRejected() throws Exception {
        String email = "flow@example.com";
        register(email, "password1");
        String activationToken = findActivationToken(email);

        mockMvc.perform(get("/api/auth/activate-account").param("token", activationToken))
                .andExpect(status().isOk());

        String jwt = login(email, "password1");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_rejected_whenAccountNotActivated() throws Exception {
        String email = "unactivated@example.com";
        register(email, "password1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_rejected_whenAccountLocked() throws Exception {
        String email = "locked@example.com";
        register(email, "password1");
        mockMvc.perform(get("/api/auth/activate-account").param("token", findActivationToken(email)))
                .andExpect(status().isOk());

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setAccountLocked(true);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_rejected_whenPasswordWrong() throws Exception {
        String email = "wrongpass@example.com";
        register(email, "password1");
        mockMvc.perform(get("/api/auth/activate-account").param("token", findActivationToken(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void activateAccount_rejected_whenTokenExpired() throws Exception {
        String email = "expiredtoken@example.com";
        register(email, "password1");
        ActivationToken activationToken = findActivationTokenEntity(email);
        activationToken.setExpiresAt(Instant.now().minusSeconds(60));
        activationTokenRepository.save(activationToken);

        mockMvc.perform(get("/api/auth/activate-account").param("token", activationToken.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activateAccount_rejected_whenTokenAlreadyUsed() throws Exception {
        String email = "reusedtoken@example.com";
        register(email, "password1");
        String token = findActivationToken(email);

        mockMvc.perform(get("/api/auth/activate-account").param("token", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/activate-account").param("token", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_allowsLoginWithNewPassword_andCannotBeReused() throws Exception {
        String email = "reset@example.com";
        register(email, "password1");
        mockMvc.perform(get("/api/auth/activate-account").param("token", findActivationToken(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email))))
                .andExpect(status().isOk());
        String resetToken = findPasswordResetToken(email);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(resetToken, "newPassword1"))))
                .andExpect(status().isOk());

        login(email, "newPassword1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(resetToken, "anotherPassword1"))))
                .andExpect(status().isBadRequest());
    }

    private void register(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private String findActivationToken(String email) {
        return findActivationTokenEntity(email).getToken();
    }

    private ActivationToken findActivationTokenEntity(String email) {
        Long userId = userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        return activationTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow();
    }

    private String findPasswordResetToken(String email) {
        Long userId = userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        return passwordResetTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .map(PasswordResetToken::getToken)
                .findFirst()
                .orElseThrow();
    }
}
