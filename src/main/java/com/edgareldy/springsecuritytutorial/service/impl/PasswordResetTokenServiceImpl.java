package com.edgareldy.springsecuritytutorial.service.impl;

import com.edgareldy.springsecuritytutorial.entity.PasswordResetToken;
import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.exception.InvalidTokenException;
import com.edgareldy.springsecuritytutorial.repository.PasswordResetTokenRepository;
import com.edgareldy.springsecuritytutorial.security.SecureTokenGenerator;
import com.edgareldy.springsecuritytutorial.service.PasswordResetTokenService;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link PasswordResetTokenService} implementation backed by
 * {@link PasswordResetTokenRepository}. A token is single-use: it is
 * deleted as soon as it is looked up in {@link #validateAndConsume}, valid
 * or not, so it can never be replayed.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final Duration EXPIRATION = Duration.ofHours(1);
    private static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SecureTokenGenerator secureTokenGenerator;

    @Override
    @Transactional
    public String generate(User user) {
        String rawToken = secureTokenGenerator.generate();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token(rawToken)
                .type(TYPE_PASSWORD_RESET)
                .expiryDate(Instant.now().plus(EXPIRATION))
                .build();
        passwordResetTokenRepository.save(passwordResetToken);
        return rawToken;
    }

    @Override
    @Transactional
    public User validateAndConsume(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));
        passwordResetTokenRepository.delete(passwordResetToken);
        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Password reset token expired");
        }
        return passwordResetToken.getUser();
    }
}
