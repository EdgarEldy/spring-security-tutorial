package com.edgareldy.springsecuritytutorial.exception;

import com.edgareldy.springsecuritytutorial.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler that translates every exception thrown by a
 * controller (directly or through a service) into a consistent
 * {@code ApiResponse<ProblemDetail>}, so callers never have to parse a
 * different error shape depending on which endpoint failed.
 * <p>
 * Created by edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleLocked(
            LockedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Account is locked", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Account is not activated", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        ProblemDetail problemDetail = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed", request);
        problemDetail.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Validation failed", problemDetail, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String message, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    private ResponseEntity<ApiResponse<ProblemDetail>> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(status, message, request);
        return ResponseEntity.status(status).body(new ApiResponse<>(false, message, problemDetail, Instant.now()));
    }
}
