package edgareldy.springsecuritytutorial.scheduler;

import edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import edgareldy.springsecuritytutorial.repository.BlacklistedTokenRepository;
import edgareldy.springsecuritytutorial.repository.PasswordResetTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily job purging expired activation tokens, password reset tokens, and
 * blacklisted JWT entries, so these tables do not grow unbounded.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        Instant now = Instant.now();
        activationTokenRepository.deleteAllByExpiresAtBefore(now);
        passwordResetTokenRepository.deleteAllByExpiryDateBefore(now);
        blacklistedTokenRepository.deleteAllByExpiresAtBefore(now);
        log.info("Purged expired activation, password reset, and blacklisted tokens");
    }
}
