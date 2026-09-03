package com.edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import com.edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import com.edgareldy.springsecuritytutorial.repository.UserRepository;
import com.edgareldy.springsecuritytutorial.service.ActivationTokenService;
import com.edgareldy.springsecuritytutorial.service.BlacklistedTokenService;
import com.edgareldy.springsecuritytutorial.service.EmailService;
import com.edgareldy.springsecuritytutorial.service.PasswordResetTokenService;
import com.edgareldy.springsecuritytutorial.service.UserService;
import com.edgareldy.springsecuritytutorial.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link AuthServiceImpl}, with every collaborator mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivationTokenService activationTokenService;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private BlacklistedTokenService blacklistedTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_createsUserAndSendsActivationEmail() {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "jane@example.com", "password1");
        UserResponse created = new UserResponse(1L, "Jane", "Doe", "jane@example.com", false, false, List.of());
        when(userService.createUser(any(UserRequest.class))).thenReturn(created);
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));
        when(activationTokenService.generate(user)).thenReturn("activation-token");

        UserResponse result = authService.register(request);

        assertThat(result).isEqualTo(created);
        verify(emailService).sendActivationEmail(user, "activation-token");
    }

    @Test
    void activateAccount_validatesTokenThenEnablesAccount() {
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(activationTokenService.validate("token")).thenReturn(user);

        authService.activateAccount("token");

        verify(userService).enableAccount(1L);
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        User user = User.builder().id(1L).email("jane@example.com").build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("jane@example.com", "password1"));

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void logout_blacklistsToken() {
        when(jwtService.extractJti("raw-token")).thenReturn("jti-1");
        when(jwtService.extractUsername("raw-token")).thenReturn("jane@example.com");
        Instant expiresAt = Instant.now().plusSeconds(60);
        when(jwtService.extractExpiration("raw-token")).thenReturn(expiresAt);
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));

        authService.logout("raw-token");

        verify(blacklistedTokenService).blacklist(user, "raw-token", "jti-1", expiresAt);
    }

    @Test
    void me_returnsCurrentAuthenticatedUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane@example.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserResponse response = new UserResponse(1L, "Jane", "Doe", "jane@example.com", true, false, List.of());
        when(userService.findByEmail("jane@example.com")).thenReturn(response);

        assertThat(authService.me()).isEqualTo(response);
    }

    @Test
    void forgotPassword_generatesTokenAndSendsEmail() {
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenService.generate(user)).thenReturn("reset-token");

        authService.forgotPassword("jane@example.com");

        verify(emailService).sendPasswordResetEmail(user, "reset-token");
    }

    @Test
    void forgotPassword_throws_whenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resetPassword_consumesTokenThenUpdatesPassword() {
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(passwordResetTokenService.validateAndConsume("token")).thenReturn(user);

        authService.resetPassword("token", "newPassword1");

        verify(userService).updatePassword(1L, "newPassword1");
    }
}
