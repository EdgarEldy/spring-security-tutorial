package com.edgareldy.springsecuritytutorial.security;

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
    void _01_ShouldReturnTrue_WhenAuthorityPresent() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:CREATE");

        assertThat(evaluator.hasPermission(authentication, "USER", "CREATE")).isTrue();
    }

    @Test
    void _02_ShouldMatchIgnoringCase_WhenResourceAndActionCaseDiffers() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:CREATE");

        assertThat(evaluator.hasPermission(authentication, "user", "create")).isTrue();
    }

    @Test
    void _03_ShouldReturnFalse_WhenAuthorityAbsent() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:READ");

        assertThat(evaluator.hasPermission(authentication, "USER", "CREATE")).isFalse();
    }

    @Test
    void _04_ShouldReturnFalse_WhenAuthenticationIsNull() {
        assertThat(evaluator.hasPermission(null, "USER", "CREATE")).isFalse();
    }

    @Test
    void _05_ShouldReturnFalse_WhenTargetDomainObjectIsNull() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:CREATE");

        assertThat(evaluator.hasPermission(authentication, null, "CREATE")).isFalse();
    }

    @Test
    void _06_ShouldReturnFalse_WhenPermissionIsNull() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:CREATE");

        assertThat(evaluator.hasPermission(authentication, "USER", null)).isFalse();
    }

    @Test
    void _07_ShouldDelegateToResourceActionCheck_WhenTargetIdOverloadUsed() {
        Authentication authentication = authenticationWithAuthorities("PERMISSION_USER:CREATE");

        assertThat(evaluator.hasPermission(authentication, 1L, "USER", "CREATE")).isTrue();
    }

    private Authentication authenticationWithAuthorities(String... authorities) {
        return new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "N/A",
                List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList());
    }
}
