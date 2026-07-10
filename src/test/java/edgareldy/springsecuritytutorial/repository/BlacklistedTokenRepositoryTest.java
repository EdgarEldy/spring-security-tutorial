package edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springsecuritytutorial.entity.BlacklistedToken;
import edgareldy.springsecuritytutorial.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link BlacklistedTokenRepository}, backed by a
 * real PostgreSQL instance via Testcontainers.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestcontainersConfiguration.class)
class BlacklistedTokenRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .password("hashed").enabled(true).accountLocked(false).build());
        blacklistedTokenRepository.save(BlacklistedToken.builder()
                .user(user).token("raw.jwt.value").jti("jti-123")
                .blacklistedAt(Instant.now()).createdAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());
    }

    @Test
    void existsByJtiReflectsCurrentData() {
        assertThat(blacklistedTokenRepository.existsByJti("jti-123")).isTrue();
        assertThat(blacklistedTokenRepository.existsByJti("unknown-jti")).isFalse();
    }

    @Test
    void deleteAllByExpiresAtBeforeRemovesOnlyExpiredEntries() {
        BlacklistedToken expired = blacklistedTokenRepository.save(BlacklistedToken.builder()
                .user(user).token("expired.jwt.value").jti("jti-expired")
                .blacklistedAt(Instant.now()).createdAt(Instant.now())
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build());

        blacklistedTokenRepository.deleteAllByExpiresAtBefore(Instant.now());

        assertThat(blacklistedTokenRepository.findById(expired.getId())).isEmpty();
        assertThat(blacklistedTokenRepository.existsByJti("jti-123")).isTrue();
    }
}
