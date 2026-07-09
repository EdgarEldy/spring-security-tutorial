package edgareldy.springsecuritytutorial.service;

import edgareldy.springsecuritytutorial.entity.User;
import java.time.Instant;

/**
 * Contract for JWT blacklisting on logout. No controller exposes this
 * directly: feature/auth's {@code AuthServiceImpl.logout} records the
 * current JWT, and {@code JwtAuthFilter} checks {@link #isBlacklisted}
 * before trusting an otherwise valid token.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface BlacklistedTokenService {

    void blacklist(User user, String rawJwt, String jti, Instant expiresAt);

    boolean isBlacklisted(String jti);
}
