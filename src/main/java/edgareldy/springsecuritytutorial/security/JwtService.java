package edgareldy.springsecuritytutorial.security;

import edgareldy.springsecuritytutorial.entity.Permission;
import edgareldy.springsecuritytutorial.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates and parses the JWTs issued on login and consumed by
 * {@code JwtAuthFilter} on every subsequent request. Claims: {@code sub}
 * (email), {@code jti} (random id, used for blacklisting on logout),
 * {@code roles}, {@code permissions}, {@code iat}, {@code exp}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().toUpperCase(Locale.ROOT))
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(this::permissionClaim)
                .distinct()
                .toList();
        return Jwts.builder()
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    private String permissionClaim(Permission permission) {
        return permission.getResource().toUpperCase(Locale.ROOT) + ":" + permission.getAction().toUpperCase(Locale.ROOT);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
