package edgareldy.springsecuritytutorial.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import edgareldy.springsecuritytutorial.repository.BlacklistedTokenRepository;
import edgareldy.springsecuritytutorial.repository.PasswordResetTokenRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link TokenCleanupScheduler}, with every repository
 * mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Test
    void purgeExpiredTokensDelegatesToEveryRepository() {
        tokenCleanupScheduler.purgeExpiredTokens();

        verify(activationTokenRepository).deleteAllByExpiresAtBefore(any(Instant.class));
        verify(passwordResetTokenRepository).deleteAllByExpiryDateBefore(any(Instant.class));
        verify(blacklistedTokenRepository).deleteAllByExpiresAtBefore(any(Instant.class));
    }
}
