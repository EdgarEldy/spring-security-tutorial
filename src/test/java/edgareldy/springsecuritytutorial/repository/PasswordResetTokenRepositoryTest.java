package edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springsecuritytutorial.entity.PasswordResetToken;
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
 * {@code @DataJpaTest} for {@link PasswordResetTokenRepository}, backed by a
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
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .password("hashed").enabled(true).accountLocked(false).build());
    }

    @Test
    void findByTokenReturnsMatchingToken() {
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user).token("raw-token").type("PASSWORD_RESET")
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        assertThat(passwordResetTokenRepository.findByToken("raw-token"))
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getUser)
                .isEqualTo(user);
    }

    @Test
    void findByTokenReturnsEmptyWhenMissing() {
        assertThat(passwordResetTokenRepository.findByToken("unknown")).isEmpty();
    }

    @Test
    void deleteAllByExpiryDateBeforeRemovesOnlyExpiredTokens() {
        PasswordResetToken expired = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user).token("expired-token").type("PASSWORD_RESET")
                .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .build());
        PasswordResetToken active = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user).token("active-token").type("PASSWORD_RESET")
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        passwordResetTokenRepository.deleteAllByExpiryDateBefore(Instant.now());

        assertThat(passwordResetTokenRepository.findById(expired.getId())).isEmpty();
        assertThat(passwordResetTokenRepository.findById(active.getId())).isPresent();
    }
}
