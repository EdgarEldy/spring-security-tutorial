package com.edgareldy.springsecuritytutorial.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Unit tests verifying that {@link GlobalExceptionHandler} maps each
 * exception type to the HTTP status and {@link ApiResponse}-wrapped
 * {@link ProblemDetail} shape the README's error-handling contract
 * requires.
 * <p>
 * Created by edgar.muhamyangabo on 9/3/26
 * Author : edgar.muhamyangabo
 * Date : 9/3/26
 * Project : spring-security-tutorial
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundMapsTo404() {
        HttpServletRequest request = mockRequest("/api/users/99");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("User not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("User not found");
        assertThat(response.getBody().data().getInstance()).isEqualTo(URI.create("/api/users/99"));
        assertThat(response.getBody().data().getStatus()).isEqualTo(404);
        assertThat(response.getBody().data().getDetail()).isEqualTo("User not found");
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @Test
    void businessRuleMapsTo422() {
        HttpServletRequest request = mockRequest("/api/users/1/lock");

        ResponseEntity<ApiResponse<ProblemDetail>> response = handler.handleBusinessRule(
                new BusinessRuleException("An admin cannot lock their own account"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().data().getStatus()).isEqualTo(422);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
        assertThat(response.getBody().data().getInstance()).isEqualTo(URI.create("/api/users/1/lock"));
    }

    @Test
    void invalidTokenMapsTo400() {
        HttpServletRequest request = mockRequest("/api/auth/activate-account");

        ResponseEntity<ApiResponse<ProblemDetail>> response = handler.handleInvalidToken(
                new InvalidTokenException("Activation token has expired"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().data().getStatus()).isEqualTo(400);
        assertThat(response.getBody().data().getDetail()).isEqualTo("Activation token has expired");
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void accessDeniedMapsTo403() {
        HttpServletRequest request = mockRequest("/api/users/1");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleAccessDenied(new AccessDeniedException("Access is denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Access denied");
        assertThat(response.getBody().data().getStatus()).isEqualTo(403);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(response.getBody().data().getInstance()).isEqualTo(URI.create("/api/users/1"));
    }

    @Test
    void badCredentialsMapsTo401() {
        HttpServletRequest request = mockRequest("/api/auth/login");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleBadCredentials(new BadCredentialsException("Bad credentials"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().data().getStatus()).isEqualTo(401);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    @Test
    void lockedAccountMapsTo401() {
        HttpServletRequest request = mockRequest("/api/auth/login");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleLocked(new LockedException("Account is locked"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Account is locked");
        assertThat(response.getBody().data().getStatus()).isEqualTo(401);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    @Test
    void disabledAccountMapsTo401() {
        HttpServletRequest request = mockRequest("/api/auth/login");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleDisabled(new DisabledException("Account is not activated"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Account is not activated");
        assertThat(response.getBody().data().getStatus()).isEqualTo(401);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    @Test
    void validationErrorsMapTo400WithFieldErrors() {
        HttpServletRequest request = mockRequest("/api/users");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userRequest");
        bindingResult.addError(new FieldError("userRequest", "firstName", "First name must not be blank"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ApiResponse<ProblemDetail>> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors =
                (Map<String, String>) response.getBody().data().getProperties().get("fieldErrors");
        assertThat(fieldErrors).hasSize(1).containsEntry("firstName", "First name must not be blank");
    }

    @Test
    void genericExceptionMapsTo500() {
        HttpServletRequest request = mockRequest("/api/users");

        ResponseEntity<ApiResponse<ProblemDetail>> response =
                handler.handleGeneric(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().data().getStatus()).isEqualTo(500);
        assertThat(response.getBody().data().getTitle()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private HttpServletRequest mockRequest(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
