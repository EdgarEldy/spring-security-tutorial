package com.edgareldy.springsecuritytutorial.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.service.BlacklistedTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link JwtAuthFilter}, with {@link JwtService},
 * {@link BlacklistedTokenService} and {@link UserDetailsServiceImpl}
 * mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private BlacklistedTokenService blacklistedTokenService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_authenticates_whenTokenValidAndNotBlacklisted() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.extractJti("valid-token")).thenReturn("jti-1");
        when(blacklistedTokenService.isBlacklisted("jti-1")).thenReturn(false);
        when(jwtService.extractUsername("valid-token")).thenReturn("jane@example.com");
        User user = User.builder().id(1L).email("jane@example.com").build();
        when(userDetailsService.loadUserByUsername("jane@example.com")).thenReturn(user);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthentication_whenTokenBlacklisted() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer blacklisted-token");
        when(jwtService.extractJti("blacklisted-token")).thenReturn("jti-2");
        when(blacklistedTokenService.isBlacklisted("jti-2")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthentication_whenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractJti(org.mockito.ArgumentMatchers.any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthentication_whenTokenInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        when(jwtService.extractJti("malformed-token")).thenThrow(new io.jsonwebtoken.MalformedJwtException("bad token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
