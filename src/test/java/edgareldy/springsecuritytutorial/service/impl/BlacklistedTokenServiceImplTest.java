package edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springsecuritytutorial.entity.BlacklistedToken;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.repository.BlacklistedTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link BlacklistedTokenServiceImpl}, with
 * {@link BlacklistedTokenRepository} mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class BlacklistedTokenServiceImplTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private BlacklistedTokenServiceImpl blacklistedTokenService;

    @Test
    void blacklistSavesEntryWithGivenJtiAndExpiry() {
        User user = User.builder().id(1L).email("ada@example.com").build();
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

        blacklistedTokenService.blacklist(user, "raw.jwt.value", "jti-123", expiresAt);

        ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getToken()).isEqualTo("raw.jwt.value");
        assertThat(captor.getValue().getJti()).isEqualTo("jti-123");
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
        assertThat(captor.getValue().getBlacklistedAt()).isNotNull();
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void isBlacklistedDelegatesToRepository() {
        when(blacklistedTokenRepository.existsByJti("jti-123")).thenReturn(true);
        when(blacklistedTokenRepository.existsByJti("jti-456")).thenReturn(false);

        assertThat(blacklistedTokenService.isBlacklisted("jti-123")).isTrue();
        assertThat(blacklistedTokenService.isBlacklisted("jti-456")).isFalse();
    }
}
