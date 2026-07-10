package edgareldy.springsecuritytutorial.repository;

import edgareldy.springsecuritytutorial.entity.BlacklistedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link BlacklistedToken}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByJti(String jti);

    void deleteAllByExpiresAtBefore(Instant instant);
}
