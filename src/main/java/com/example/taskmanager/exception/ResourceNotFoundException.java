package com.example.taskmanager.exception;

public class ResourceNotFoundException extends TaskManagerException {
    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }
}