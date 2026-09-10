package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String PROBLEM_JSON_TYPE = "application/problem+json";

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ProblemDetails> handleDuplicateHotelException(
            DuplicateException ex,
            WebRequest request) {

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/conflict")
                .title("Resource Conflict")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ProblemDetails> handleInvalidPasswordException(
            InvalidPasswordException ex,
            WebRequest request) {

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/unauthorized")
                .title("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetails> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/forbidden")
                .title("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You don't have permission to access this resource")
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetails> handleRuntimeException(
            RuntimeException e,
            WebRequest request) {

        log.error("Runtime exception: {}", e.getMessage());
        // Не выводим stacktrace в ответе
        log.debug("Stacktrace: ", e);

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/bad-request")
                .title("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Invalid request: " + e.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationException(
            MethodArgumentNotValidException e,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("validationErrors", errors);
        additionalInfo.put("errorCount", errors.size());

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/validation")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed. Please check the provided data.")
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .additionalInfo(additionalInfo)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleException(
            Exception e,
            WebRequest request) {

        log.error("Unexpected error", e); // Логируем полный stacktrace в лог
        // Не выводим e.getMessage() в ответе клиенту
        // и не передаем детали реализации

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type("https://example.com/errors/internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.parseMediaType(PROBLEM_JSON_TYPE))
                .body(problemDetails);
    }
}