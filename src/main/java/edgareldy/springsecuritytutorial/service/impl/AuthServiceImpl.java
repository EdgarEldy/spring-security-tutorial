package edgareldy.springsecuritytutorial.service.impl;

import edgareldy.springsecuritytutorial.dto.auth.AuthResponse;
import edgareldy.springsecuritytutorial.dto.auth.LoginRequest;
import edgareldy.springsecuritytutorial.dto.auth.RegisterRequest;
import edgareldy.springsecuritytutorial.dto.user.UserRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import edgareldy.springsecuritytutorial.repository.UserRepository;
import edgareldy.springsecuritytutorial.service.ActivationTokenService;
import edgareldy.springsecuritytutorial.service.AuthService;
import edgareldy.springsecuritytutorial.service.BlacklistedTokenService;
import edgareldy.springsecuritytutorial.service.EmailService;
import edgareldy.springsecuritytutorial.service.PasswordResetTokenService;
import edgareldy.springsecuritytutorial.service.UserService;
import edgareldy.springsecuritytutorial.security.JwtService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link AuthService} implementation. Orchestrates
 * {@link UserService}, {@link ActivationTokenService},
 * {@link PasswordResetTokenService}, {@link BlacklistedTokenService},
 * {@link EmailService}, and {@link JwtService}; also reads
 * {@link UserRepository} directly where an actual {@link User} entity is
 * needed (the token/email services take entities, while
 * {@link UserService} only ever returns DTOs).
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ActivationTokenService activationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final BlacklistedTokenService blacklistedTokenService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        UserResponse created = userService.createUser(
                new UserRequest(request.firstName(), request.lastName(), request.email(), request.password()));
        User user = getUserOrThrow(created.email());
        String token = activationTokenService.generate(user);
        emailService.sendActivationEmail(user, token);
        return created;
    }

    @Override
    @Transactional
    public void activateAccount(String token) {
        User user = activationTokenService.validate(token);
        userService.enableAccount(user.getId());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = (User) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(user));
    }

    @Override
    @Transactional
    public void logout(String rawToken) {
        String jti = jwtService.extractJti(rawToken);
        String email = jwtService.extractUsername(rawToken);
        Instant expiresAt = jwtService.extractExpiration(rawToken);
        User user = getUserOrThrow(email);
        blacklistedTokenService.blacklist(user, rawToken, jti, expiresAt);
    }

    @Override
    public UserResponse me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = getUserOrThrow(email);
        String token = passwordResetTokenService.generate(user);
        emailService.sendPasswordResetEmail(user, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = passwordResetTokenService.validateAndConsume(token);
        userService.updatePassword(user.getId(), newPassword);
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }
}
