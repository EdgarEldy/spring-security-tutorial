package com.edgareldy.springsecuritytutorial.scheduler;

import com.edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import com.edgareldy.springsecuritytutorial.repository.BlacklistedTokenRepository;
import com.edgareldy.springsecuritytutorial.repository.PasswordResetTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily job purging expired activation tokens, password reset tokens, and
 * blacklisted JWT entries, so these tables do not grow unbounded. Each
 * table is purged by its own {@code @Scheduled} method and its own
 * transaction, rather than one method purging all three in a single
 * transaction: none of these tables depend on one another, so a failure
 * purging one (e.g. a lock timeout) should not roll back the other two,
 * which would otherwise silently never get cleaned up either.
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
    private static final String DAILY_AT_3AM = "0 0 3 * * *";

    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = DAILY_AT_3AM)
    @Transactional
    public void purgeExpiredActivationTokens() {
        activationTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        log.info("Purged expired activation tokens");
    }

    @Scheduled(cron = DAILY_AT_3AM)
    @Transactional
    public void purgeExpiredPasswordResetTokens() {
        passwordResetTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
        log.info("Purged expired password reset tokens");
    }

    @Scheduled(cron = DAILY_AT_3AM)
    @Transactional
    public void purgeExpiredBlacklistedTokens() {
        blacklistedTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        log.info("Purged expired blacklisted tokens");
    }
}
