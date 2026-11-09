package com.varun.taskforge.shared.web;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.varun.taskforge.task.exception.TaskInterruptedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed");
    }

    @ExceptionHandler(TaskInterruptedException.class)
    public ResponseEntity<ErrorResponseDTO> handleInterrupted(TaskInterruptedException ex) {
        log.warn(ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "TASK_INTERRUPTED", ex.getMessage());
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String error, String message) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
