package com.github.deniskoriavets.sportvision.exception;

import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicConflict(
        IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Business rule violation",
            ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error",
            "Please check the provided data", request.getRequestURI(), errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(),
            request.getRequestURI(), null);
    }

    @ExceptionHandler({
        EmailAlreadyTakenException.class,
        EmailAlreadyVerifiedException.class,
        ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleConflicts(
        RuntimeException ex, HttpServletRequest request) {

        String message = ex.getMessage();
        if (ex instanceof ObjectOptimisticLockingFailureException) {
            message =
                "Data was modified by another user or process. Please refresh and try again.";
        }

        return buildResponse(HttpStatus.CONFLICT, "Data conflict", message,
            request.getRequestURI(), null);
    }

    @ExceptionHandler({
        InvalidCredentialsException.class,
        InvalidTokenException.class,
        TokenExpiredException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(
        RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication error", ex.getMessage(),
            request.getRequestURI(), null);
    }

    @ExceptionHandler({
        EmailNotVerifiedException.class,
        AccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(
        RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage(),
            request.getRequestURI(), null);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSending(
        EmailSendingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "External service error",
            ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(
        Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
            "An unexpected error occurred. We are already working on it.",
            request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
        HttpStatus status, String error, String message, String path,
        Map<String, String> validationErrors) {

        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            status.value(),
            error,
            message,
            path,
            validationErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}