package dev.io.tracebit.exception;

import dev.io.tracebit.dto.response.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetails buildProblem(HttpStatus status, String error, String message, String path) {
        return ProblemDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    private ProblemDetails buildProblemWithErrors(HttpStatus status, String error, String message, String path, Map<String, String> errors) {
        return ProblemDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        return ResponseEntity
                .badRequest()
                .body(buildProblemWithErrors(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request.getRequestURI(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetails> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        return ResponseEntity
                .badRequest()
                .body(buildProblemWithErrors(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", "Constraint violation", request.getRequestURI(), errors));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetails> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblem(HttpStatus.BAD_REQUEST, "MISSING_HEADER", "Required header missing: " + ex.getHeaderName(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetails> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblem(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", "Invalid parameter type for: " + ex.getName(), request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetails> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_FORMAT", "Malformed JSON request", request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblem(HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation", ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildProblem(HttpStatus.CONFLICT, "DATA_INTEGRITY", "Data conflict or constraint violation", request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetails> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildProblem(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ProblemDetails> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildProblem(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildProblem(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetails> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblem(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetails> handleCustomValidation(ValidationException ex, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(buildProblemWithErrors(HttpStatus.BAD_REQUEST, "VALIDATION_EXCEPTION", ex.getMessage(), request.getRequestURI(), ex.getErrors()));
    }
}
