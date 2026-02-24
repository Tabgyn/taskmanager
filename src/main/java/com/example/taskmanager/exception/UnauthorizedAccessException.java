package com.example.taskmanager.exception;

public class UnauthorizedAccessException extends TaskManagerException {
    public UnauthorizedAccessException() {
        super("You do not have permission to access this resource");
    }
}