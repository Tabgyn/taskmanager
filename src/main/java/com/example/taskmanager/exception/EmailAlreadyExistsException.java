package com.example.taskmanager.exception;

public class EmailAlreadyExistsException extends TaskManagerException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}