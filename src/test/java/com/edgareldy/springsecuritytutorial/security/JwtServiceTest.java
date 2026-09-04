package com.edgareldy.springsecuritytutorial.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgareldy.springsecuritytutorial.entity.Permission;
import com.edgareldy.springsecuritytutorial.entity.Role;
import com.edgareldy.springsecuritytutorial.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JwtService}.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L, new SecureTokenGenerator());
    }

    @Test
    void generateToken_embedsSubjectRolesAndPermissions() {
        Permission permission = Permission.builder().resource("user").action("create").build();
        Role role = Role.builder().roleName("admin").permissions(Set.of(permission)).build();
        User user = User.builder().email("jane@example.com").roles(Set.of(role)).build();

        String token = jwtService.generateToken(user);

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo("jane@example.com");
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.get("roles", List.class)).containsExactly("ADMIN");
        assertThat(claims.get("permissions", List.class)).containsExactly("USER:CREATE");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void extractUsername_returnsSubject() {
        User user = User.builder().email("jane@example.com").roles(Set.of()).build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jane@example.com");
    }

    @Test
    void extractJti_returnsUniqueIdPerToken() {
        User user = User.builder().email("jane@example.com").roles(Set.of()).build();

        String first = jwtService.generateToken(user);
        String second = jwtService.generateToken(user);

        assertThat(jwtService.extractJti(first)).isNotEqualTo(jwtService.extractJti(second));
    }

    @Test
    void extractExpiration_isAfterNow() {
        User user = User.builder().email("jane@example.com").roles(Set.of()).build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractExpiration(token)).isAfter(Instant.now());
    }
}
