package com.example.taskmanager.exception;

import java.time.LocalDateTime;
import java.util.List;

// Java 21 Sealed Classes — restricts which classes can extend ApiError
public sealed interface ApiError
        permits ApiError.NotFound, ApiError.Conflict, ApiError.Forbidden, ApiError.Validation {

    record NotFound(String message, LocalDateTime timestamp) implements ApiError {}
    record Conflict(String message, LocalDateTime timestamp) implements ApiError {}
    record Forbidden(String message, LocalDateTime timestamp) implements ApiError {}
    record Validation(List<String> errors, LocalDateTime timestamp) implements ApiError {}
}