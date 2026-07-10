package edgareldy.springsecuritytutorial.config;

import edgareldy.springsecuritytutorial.security.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize}/{@code @PostAuthorize} on service and
 * controller methods, independently of the HTTP filter chain that
 * feature/auth's {@code SecurityConfig} will register. UserController's
 * ADMIN-only endpoints rely on this to be enforced, and the
 * {@link MethodSecurityExpressionHandler} bean wires
 * {@link CustomPermissionEvaluator} in so
 * {@code hasPermission('RESOURCE', 'ACTION')} expressions are evaluated by
 * it instead of Spring Security's default (which always denies without a
 * configured {@code PermissionEvaluator}).
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            CustomPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
