package com.example.taskmanager.service;

import com.example.taskmanager.domain.*;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedAccessException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").name("Test").password("hashed").build();
        task = Task.builder().id(1L).title("Test Task").status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM).owner(user).build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTask() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any())).thenReturn(task);

        TaskRequest request = new TaskRequest("Test Task", null, null, null, null);
        TaskResponse response = taskService.create(request, 1L);

        assertThat(response.title()).isEqualTo("Test Task");
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("Should throw when user not found on create")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        TaskRequest request = new TaskRequest("Task", null, null, null, null);

        assertThatThrownBy(() -> taskService.create(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find task by id")
    void shouldFindById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(1L, 1L);
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw when task not found")
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when user does not own task")
    void shouldThrowOnUnauthorizedAccess() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(1L, 99L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("Should update task")
    void shouldUpdateTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskRequest request = new TaskRequest("Updated", null, TaskStatus.IN_PROGRESS, null, null);
        TaskResponse response = taskService.update(1L, request, 1L);

        assertThat(response).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should delete task")
    void shouldDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L, 1L);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("Should return paged tasks by owner")
    void shouldReturnPagedTasks() {
        Page<Task> pageResult = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
        when(taskRepository.findByOwnerIdWithFilters(eq(1L), isNull(), any())).thenReturn(pageResult);

        var response = taskService.findByOwner(1L, null, 0, 10);
        assertThat(response.totalElements()).isEqualTo(1);
    }
}