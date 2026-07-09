package edgareldy.springsecuritytutorial.service.impl;

import edgareldy.springsecuritytutorial.entity.ActivationToken;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.exception.InvalidTokenException;
import edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import edgareldy.springsecuritytutorial.security.SecureTokenGenerator;
import edgareldy.springsecuritytutorial.service.ActivationTokenService;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link ActivationTokenService} implementation backed by
 * {@link ActivationTokenRepository}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class ActivationTokenServiceImpl implements ActivationTokenService {

    private static final Duration EXPIRATION = Duration.ofHours(24);

    private final ActivationTokenRepository activationTokenRepository;
    private final SecureTokenGenerator secureTokenGenerator;

    @Override
    @Transactional
    public String generate(User user) {
        String rawToken = secureTokenGenerator.generate();
        Instant now = Instant.now();
        ActivationToken activationToken = ActivationToken.builder()
                .user(user)
                .token(rawToken)
                .createdAt(now)
                .expiresAt(now.plus(EXPIRATION))
                .build();
        activationTokenRepository.save(activationToken);
        return rawToken;
    }

    @Override
    @Transactional
    public User validate(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid activation token"));
        if (activationToken.getValidatedAt() != null) {
            throw new InvalidTokenException("Activation token already used");
        }
        if (activationToken.getExpiresAt() != null && activationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Activation token expired");
        }
        activationToken.setValidatedAt(Instant.now());
        activationTokenRepository.save(activationToken);
        return activationToken.getUser();
    }
}
