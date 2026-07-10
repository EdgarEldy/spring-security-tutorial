package edgareldy.springsecuritytutorial.service;

import edgareldy.springsecuritytutorial.dto.auth.AuthResponse;
import edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import edgareldy.springsecuritytutorial.security.JwtService;

/**
 * Contract orchestrating the full authentication flow: registration,
 * account activation, login, logout, current-user profile, and password
 * reset. Delegates the actual work to {@link UserService},
 * {@link ActivationTokenService}, {@link PasswordResetTokenService},
 * {@link BlacklistedTokenService}, {@link EmailService}, and
 * {@link JwtService}; this interface is the only thing
 * {@code AuthController} depends on.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
public interface AuthService {

    UserResponse register(RegisterRequest request);

    void activateAccount(String token);

    AuthResponse login(LoginRequest request);

    void logout(String rawToken);

    UserResponse me();

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
