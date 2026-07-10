package edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springsecuritytutorial.entity.ActivationToken;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.exception.InvalidTokenException;
import edgareldy.springsecuritytutorial.repository.ActivationTokenRepository;
import edgareldy.springsecuritytutorial.security.SecureTokenGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ActivationTokenServiceImpl}, with
 * {@link ActivationTokenRepository} and {@link SecureTokenGenerator} mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class ActivationTokenServiceImplTest {

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @Mock
    private SecureTokenGenerator secureTokenGenerator;

    @InjectMocks
    private ActivationTokenServiceImpl activationTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("ada@example.com").build();
    }

    @Test
    void generateSavesTokenAndReturnsRawValue() {
        when(secureTokenGenerator.generate()).thenReturn("raw-token");

        String result = activationTokenService.generate(user);

        assertThat(result).isEqualTo("raw-token");
        ArgumentCaptor<ActivationToken> captor = ArgumentCaptor.forClass(ActivationToken.class);
        verify(activationTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getToken()).isEqualTo("raw-token");
        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now());
        assertThat(captor.getValue().getValidatedAt()).isNull();
    }

    @Test
    void validateMarksTokenValidatedAndReturnsUser() {
        ActivationToken activationToken = ActivationToken.builder()
                .id(1L).user(user).token("raw-token")
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        when(activationTokenRepository.findByToken("raw-token")).thenReturn(Optional.of(activationToken));

        User result = activationTokenService.validate("raw-token");

        assertThat(result).isEqualTo(user);
        assertThat(activationToken.getValidatedAt()).isNotNull();
        verify(activationTokenRepository).save(activationToken);
    }

    @Test
    void validateThrowsWhenTokenMissing() {
        when(activationTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activationTokenService.validate("missing"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void validateThrowsWhenAlreadyValidated() {
        ActivationToken activationToken = ActivationToken.builder()
                .id(1L).user(user).token("raw-token")
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .validatedAt(Instant.now())
                .build();
        when(activationTokenRepository.findByToken("raw-token")).thenReturn(Optional.of(activationToken));

        assertThatThrownBy(() -> activationTokenService.validate("raw-token"))
                .isInstanceOf(InvalidTokenException.class);

        verify(activationTokenRepository, never()).save(any());
    }

    @Test
    void validateThrowsWhenExpired() {
        ActivationToken activationToken = ActivationToken.builder()
                .id(1L).user(user).token("raw-token")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(activationTokenRepository.findByToken("raw-token")).thenReturn(Optional.of(activationToken));

        assertThatThrownBy(() -> activationTokenService.validate("raw-token"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
