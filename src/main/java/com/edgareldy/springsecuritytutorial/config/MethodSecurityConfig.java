package com.edgareldy.springsecuritytutorial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize}/{@code @PostAuthorize} on service and
 * controller methods, independently of the HTTP filter chain that
 * feature/auth's {@code SecurityConfig} will register. UserController's
 * ADMIN-only endpoints rely on this to be enforced.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
