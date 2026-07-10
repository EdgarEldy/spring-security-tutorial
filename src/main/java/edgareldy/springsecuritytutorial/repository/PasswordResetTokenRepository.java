package edgareldy.springsecuritytutorial.repository;

import edgareldy.springsecuritytutorial.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * Spring Data JPA repository for {@link PasswordResetToken}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Locks the matching row for the rest of the caller's transaction, so
     * two concurrent {@code validateAndConsume} calls for the same token
     * cannot both read it before either has deleted it, which would let the
     * token be used twice.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findByToken(String token);

    void deleteAllByExpiryDateBefore(Instant instant);
}
