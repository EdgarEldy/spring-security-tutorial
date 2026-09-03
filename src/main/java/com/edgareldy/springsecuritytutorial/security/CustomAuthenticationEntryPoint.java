package com.edgareldy.springsecuritytutorial.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Writes the same {@code ApiResponse<Void>} error envelope used by
 * {@code GlobalExceptionHandler} whenever an unauthenticated request hits a
 * protected endpoint. {@code GlobalExceptionHandler} only sees exceptions
 * thrown inside a controller method; a missing/invalid JWT is rejected
 * earlier, in the security filter chain, where this entry point runs
 * instead.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error("Authentication required")));
    }
}
