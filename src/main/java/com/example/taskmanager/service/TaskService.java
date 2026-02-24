package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.dto.PageResponse;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedAccessException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse create(TaskRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : com.example.taskmanager.domain.TaskPriority.MEDIUM)
                .dueDate(request.dueDate())
                .owner(owner)
                .build();

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> findByOwner(Long ownerId, TaskStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = taskRepository.findByOwnerIdWithFilters(ownerId, status, pageable);
        return PageResponse.from(result, TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long taskId, Long requestingUserId) {
        Task task = getTaskOrThrow(taskId);
        validateOwnership(task, requestingUserId);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskRequest request, Long requestingUserId) {
        Task task = getTaskOrThrow(taskId);
        validateOwnership(task, requestingUserId);

        task.setTitle(request.title());
        task.setDescription(request.description());

        // Java 21 pattern matching with null-safe updates
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long taskId, Long requestingUserId) {
        Task task = getTaskOrThrow(taskId);
        validateOwnership(task, requestingUserId);
        taskRepository.delete(task);
    }

    // Java 21 pattern matching for instanceof
    private void validateOwnership(Task task, Long userId) {
        User owner = task.getOwner();
        if (owner instanceof User u && !u.getId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }
}