package com.example.task_management_api.exception;

import com.example.task_management_api.service.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(EntityNotFoundException ex) {
        return new ApiError(Instant.now(), 404, "Not Found", ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "))
                : ex.getMessage();
        return new ApiError(Instant.now(), 400, "Bad Request", message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError malformedRequest(Exception ex) {
        return new ApiError(Instant.now(), 400, "Bad Request", "Request contains invalid values");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(DataIntegrityViolationException ex) {
        return new ApiError(Instant.now(), 409, "Conflict", "The request conflicts with existing data");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError unauthorized(AuthenticationException ex) {
        return new ApiError(Instant.now(), 401, "Unauthorized", "Invalid username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(AccessDeniedException ex) {
        return new ApiError(Instant.now(), 403, "Forbidden", "You do not have permission to access this resource");
    }

    public record ApiError(Instant timestamp, int status, String error, String message) {}
}
