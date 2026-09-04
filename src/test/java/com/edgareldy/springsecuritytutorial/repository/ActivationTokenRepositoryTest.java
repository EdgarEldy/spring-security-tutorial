package com.edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgareldy.springsecuritytutorial.entity.ActivationToken;
import com.edgareldy.springsecuritytutorial.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link ActivationTokenRepository}, backed by a
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
class ActivationTokenRepositoryTest {

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .password("hashed").enabled(false).accountLocked(false).build());
    }

    @Test
    void findByTokenReturnsMatchingToken() {
        activationTokenRepository.save(ActivationToken.builder()
                .user(user).token("raw-token")
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build());

        assertThat(activationTokenRepository.findByToken("raw-token"))
                .isPresent()
                .get()
                .extracting(ActivationToken::getUser)
                .isEqualTo(user);
    }

    @Test
    void findByTokenReturnsEmptyWhenMissing() {
        assertThat(activationTokenRepository.findByToken("unknown")).isEmpty();
    }

    @Test
    void deleteAllByExpiresAtBeforeRemovesOnlyExpiredTokens() {
        ActivationToken expired = activationTokenRepository.save(ActivationToken.builder()
                .user(user).token("expired-token")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build());
        ActivationToken active = activationTokenRepository.save(ActivationToken.builder()
                .user(user).token("active-token")
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build());

        activationTokenRepository.deleteAllByExpiresAtBefore(Instant.now());

        assertThat(activationTokenRepository.findById(expired.getId())).isEmpty();
        assertThat(activationTokenRepository.findById(active.getId())).isPresent();
    }
}
