package edgareldy.springsecuritytutorial.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Unit tests for {@link CustomPermissionEvaluator}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
class CustomPermissionEvaluatorTest {

    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator();

    @Test
    void hasPermissionReturnsTrueWhenAuthorityPresent() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_PRODUCT_WRITE");

        assertThat(evaluator.hasPermission(authentication, "PRODUCT", "WRITE")).isTrue();
    }

    @Test
    void hasPermissionIsCaseInsensitiveOnResourceAndAction() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_PRODUCT_WRITE");

        assertThat(evaluator.hasPermission(authentication, "product", "write")).isTrue();
    }

    @Test
    void hasPermissionReturnsFalseWhenAuthorityAbsent() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_PRODUCT_READ");

        assertThat(evaluator.hasPermission(authentication, "PRODUCT", "WRITE")).isFalse();
    }

    @Test
    void hasPermissionReturnsFalseWhenAuthenticationIsNull() {
        assertThat(evaluator.hasPermission(null, "PRODUCT", "WRITE")).isFalse();
    }

    @Test
    void targetIdOverloadDelegatesToResourceActionCheck() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_PRODUCT_WRITE");

        assertThat(evaluator.hasPermission(authentication, 1L, "PRODUCT", "WRITE")).isTrue();
    }

    private Authentication authenticationWithAuthorities(String... authorities) {
        return new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "N/A",
                List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList());
    }
}
