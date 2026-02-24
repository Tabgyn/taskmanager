package com.example.taskmanager.controller;

import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.PageResponse;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long userId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> findAll(
            @PathVariable Long userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.findByOwner(userId, status, page, size));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> findById(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.findById(taskId, userId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(taskId, request, userId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long taskId) {
        taskService.delete(taskId, userId);
        return ResponseEntity.noContent().build();
    }
}